package br.edu.qualidade.biblioteca.web.controller;

import br.edu.qualidade.biblioteca.domain.model.Book;
import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import br.edu.qualidade.biblioteca.service.BookService;
import br.edu.qualidade.biblioteca.service.CurrentUserService;
import br.edu.qualidade.biblioteca.service.exception.BusinessRuleException;
import br.edu.qualidade.biblioteca.web.form.BookForm;
import br.edu.qualidade.biblioteca.web.form.ProgressForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final CurrentUserService currentUserService;

    public BookController(
            BookService bookService,
            CurrentUserService currentUserService
    ) {
        this.bookService = bookService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String listBooks(
            @RequestParam(name = "q", required = false) String query,
            Model model
    ) {
        LibraryUser currentUser = currentUserService.getAuthenticatedUser();
        List<Book> books = bookService.listBooks(currentUser.getId(), query);

        model.addAttribute("books", books);
        model.addAttribute("query", query);

        return "books/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("bookForm")) {
            model.addAttribute("bookForm", new BookForm());
        }

        model.addAttribute("pageTitle", "Cadastrar livro");
        model.addAttribute("formAction", "/books/new");
        model.addAttribute("submitLabel", "Cadastrar");

        return "books/form";
    }

    @PostMapping("/new")
    public String createBook(
            @Valid @ModelAttribute("bookForm") BookForm bookForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareCreateForm(model);
            return "books/form";
        }

        LibraryUser currentUser = currentUserService.getAuthenticatedUser();

        try {
            Book createdBook = bookService.createBook(currentUser.getId(), bookForm);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Livro cadastrado com sucesso."
            );

            return "redirect:/books/" + createdBook.getId();
        } catch (BusinessRuleException exception) {
            bindingResult.addError(new ObjectError("bookForm", exception.getMessage()));
            prepareCreateForm(model);
            return "books/form";
        }
    }

    @GetMapping("/{id}")
    public String showBookDetails(
            @PathVariable String id,
            Model model
    ) {
        LibraryUser currentUser = currentUserService.getAuthenticatedUser();
        Book book = bookService.getBookForOwner(id, currentUser.getId());

        ProgressForm progressForm = new ProgressForm();
        progressForm.setCurrentPage(book.getCurrentPage());

        model.addAttribute("book", book);
        model.addAttribute("progressForm", progressForm);

        return "books/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable String id,
            Model model
    ) {
        LibraryUser currentUser = currentUserService.getAuthenticatedUser();
        Book book = bookService.getBookForOwner(id, currentUser.getId());

        model.addAttribute("bookForm", BookForm.from(book));
        model.addAttribute("pageTitle", "Editar livro");
        model.addAttribute("formAction", "/books/" + id + "/edit");
        model.addAttribute("submitLabel", "Salvar alterações");

        return "books/form";
    }

    @PostMapping("/{id}/edit")
    public String updateBook(
            @PathVariable String id,
            @Valid @ModelAttribute("bookForm") BookForm bookForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareEditForm(model, id);
            return "books/form";
        }

        LibraryUser currentUser = currentUserService.getAuthenticatedUser();

        try {
            bookService.updateBook(id, currentUser.getId(), bookForm);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Livro atualizado com sucesso."
            );

            return "redirect:/books/" + id;
        } catch (BusinessRuleException exception) {
            bindingResult.addError(new ObjectError("bookForm", exception.getMessage()));
            prepareEditForm(model, id);
            return "books/form";
        }
    }

    @PostMapping("/{id}/progress")
    public String updateProgress(
            @PathVariable String id,
            @Valid @ModelAttribute("progressForm") ProgressForm progressForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Informe uma página atual válida."
            );
            return "redirect:/books/" + id;
        }

        LibraryUser currentUser = currentUserService.getAuthenticatedUser();

        try {
            bookService.updateProgress(id, currentUser.getId(), progressForm.getCurrentPage());

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Progresso atualizado com sucesso."
            );
        } catch (BusinessRuleException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }

        return "redirect:/books/" + id;
    }

    @PostMapping("/{id}/abandon")
    public String markAsAbandoned(
            @PathVariable String id,
            RedirectAttributes redirectAttributes
    ) {
        LibraryUser currentUser = currentUserService.getAuthenticatedUser();
        bookService.markAsAbandoned(id, currentUser.getId());

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Livro marcado como abandonado."
        );

        return "redirect:/books/" + id;
    }

    @PostMapping("/{id}/to-read")
    public String markAsToRead(
            @PathVariable String id,
            RedirectAttributes redirectAttributes
    ) {
        LibraryUser currentUser = currentUserService.getAuthenticatedUser();
        bookService.markAsToRead(id, currentUser.getId());

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Livro movido para a lista de leitura."
        );

        return "redirect:/books/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(
            @PathVariable String id,
            RedirectAttributes redirectAttributes
    ) {
        LibraryUser currentUser = currentUserService.getAuthenticatedUser();
        bookService.deleteBook(id, currentUser.getId());

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Livro removido com sucesso."
        );

        return "redirect:/books";
    }

    private void prepareCreateForm(Model model) {
        model.addAttribute("pageTitle", "Cadastrar livro");
        model.addAttribute("formAction", "/books/new");
        model.addAttribute("submitLabel", "Cadastrar");
    }

    private void prepareEditForm(Model model, String bookId) {
        model.addAttribute("pageTitle", "Editar livro");
        model.addAttribute("formAction", "/books/" + bookId + "/edit");
        model.addAttribute("submitLabel", "Salvar alterações");
    }
}