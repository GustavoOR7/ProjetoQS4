package br.edu.qualidade.biblioteca.service;

import br.edu.qualidade.biblioteca.domain.model.Book;
import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import br.edu.qualidade.biblioteca.domain.repository.BookRepository;
import br.edu.qualidade.biblioteca.domain.repository.LibraryUserRepository;
import br.edu.qualidade.biblioteca.domain.valueobject.BookStatus;
import br.edu.qualidade.biblioteca.service.exception.BusinessRuleException;
import br.edu.qualidade.biblioteca.service.exception.ResourceNotFoundException;
import br.edu.qualidade.biblioteca.testsupport.AbstractMongoContainerTest;
import br.edu.qualidade.biblioteca.web.form.BookForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BookServiceIT extends AbstractMongoContainerTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LibraryUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Deve cadastrar livro para usuário real no MongoDB")
    void shouldCreateBookForOwner() {
        LibraryUser user = createUser("ana@example.com");

        Book createdBook = bookService.createBook(user.getId(), validBookForm(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                464
        ));

        assertThat(createdBook.getId()).isNotBlank();
        assertThat(createdBook.getOwnerUserId()).isEqualTo(user.getId());
        assertThat(createdBook.getStatus()).isEqualTo(BookStatus.TO_READ);

        assertThat(bookRepository.findById(createdBook.getId())).isPresent();
    }

    @Test
    @DisplayName("Deve rejeitar ISBN duplicado para o mesmo usuário")
    void shouldRejectDuplicatedIsbnForSameOwner() {
        LibraryUser user = createUser("ana@example.com");

        bookService.createBook(user.getId(), validBookForm(
                "Livro A",
                "Autor A",
                "isbn-duplicado",
                200
        ));

        BookForm duplicatedForm = validBookForm(
                "Livro B",
                "Autor B",
                "isbn-duplicado",
                300
        );

        assertThatThrownBy(() -> bookService.createBook(user.getId(), duplicatedForm))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ISBN");
    }

    @Test
    @DisplayName("Deve permitir mesmo ISBN para usuários diferentes")
    void shouldAllowSameIsbnForDifferentOwners() {
        LibraryUser userA = createUser("ana@example.com");
        LibraryUser userB = createUser("bruno@example.com");

        bookService.createBook(userA.getId(), validBookForm(
                "Livro A",
                "Autor A",
                "isbn-compartilhado",
                200
        ));

        Book bookFromUserB = bookService.createBook(userB.getId(), validBookForm(
                "Livro B",
                "Autor B",
                "isbn-compartilhado",
                300
        ));

        assertThat(bookFromUserB.getId()).isNotBlank();
        assertThat(bookRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve impedir que usuário acesse livro de outro usuário")
    void shouldPreventAccessToAnotherUserBook() {
        LibraryUser owner = createUser("owner@example.com");
        LibraryUser intruder = createUser("intruder@example.com");

        Book book = bookService.createBook(owner.getId(), validBookForm(
                "Livro privado",
                "Autor",
                "isbn-privado",
                200
        ));

        assertThatThrownBy(() -> bookService.getBookForOwner(book.getId(), intruder.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Livro não encontrado");
    }

    @ParameterizedTest
    @CsvSource({
            "0, TO_READ",
            "1, READING",
            "150, READING",
            "300, FINISHED"
    })
    @DisplayName("Deve atualizar status do livro conforme progresso")
    void shouldUpdateBookStatusAccordingToProgress(int currentPage, BookStatus expectedStatus) {
        LibraryUser user = createUser("ana@example.com");

        Book book = bookService.createBook(user.getId(), validBookForm(
                "Refactoring",
                "Martin Fowler",
                "9780201485677",
                300
        ));

        Book updatedBook = bookService.updateProgress(book.getId(), user.getId(), currentPage);

        assertThat(updatedBook.getCurrentPage()).isEqualTo(currentPage);
        assertThat(updatedBook.getStatus()).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("Deve rejeitar progresso maior que total de páginas")
    void shouldRejectProgressGreaterThanTotalPages() {
        LibraryUser user = createUser("ana@example.com");

        Book book = bookService.createBook(user.getId(), validBookForm(
                "Refactoring",
                "Martin Fowler",
                "9780201485677",
                300
        ));

        assertThatThrownBy(() -> bookService.updateProgress(book.getId(), user.getId(), 301))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("maior que o total");
    }

    @Test
    @DisplayName("Deve listar somente livros do usuário autenticado")
    void shouldListOnlyOwnerBooks() {
        LibraryUser userA = createUser("ana@example.com");
        LibraryUser userB = createUser("bruno@example.com");

        bookService.createBook(userA.getId(), validBookForm("Clean Code", "Robert C. Martin", "isbn-a", 464));
        bookService.createBook(userA.getId(), validBookForm("Refactoring", "Martin Fowler", "isbn-b", 300));
        bookService.createBook(userB.getId(), validBookForm("Livro de outro usuário", "Autor", "isbn-c", 100));

        List<Book> booksFromUserA = bookService.listBooks(userA.getId(), null);

        assertThat(booksFromUserA)
                .hasSize(2)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Refactoring");
    }

    @Test
    @DisplayName("Deve buscar livros do usuário por termo")
    void shouldSearchOwnerBooksByTerm() {
        LibraryUser user = createUser("ana@example.com");

        bookService.createBook(user.getId(), validBookForm("Clean Code", "Robert C. Martin", "isbn-a", 464));
        bookService.createBook(user.getId(), validBookForm("Refactoring", "Martin Fowler", "isbn-b", 300));
        bookService.createBook(user.getId(), validBookForm("O Hobbit", "J. R. R. Tolkien", "isbn-c", 310));

        List<Book> result = bookService.listBooks(user.getId(), "martin");

        assertThat(result)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Refactoring");
    }

    @Test
    @DisplayName("Deve marcar livro como abandonado")
    void shouldMarkBookAsAbandoned() {
        LibraryUser user = createUser("ana@example.com");

        Book book = bookService.createBook(user.getId(), validBookForm(
                "Livro difícil",
                "Autor",
                "isbn-abandonado",
                500
        ));

        Book abandonedBook = bookService.markAsAbandoned(book.getId(), user.getId());

        assertThat(abandonedBook.getStatus()).isEqualTo(BookStatus.ABANDONED);
    }

    @Test
    @DisplayName("Deve excluir livro do usuário")
    void shouldDeleteBook() {
        LibraryUser user = createUser("ana@example.com");

        Book book = bookService.createBook(user.getId(), validBookForm(
                "Livro removível",
                "Autor",
                "isbn-delete",
                100
        ));

        bookService.deleteBook(book.getId(), user.getId());

        assertThat(bookRepository.findById(book.getId())).isEmpty();
    }

    private LibraryUser createUser(String email) {
        return userRepository.save(new LibraryUser(
                "Usuário Teste",
                email,
                passwordEncoder.encode("Senha123")
        ));
    }

    private BookForm validBookForm(
            String title,
            String author,
            String isbn,
            Integer totalPages
    ) {
        BookForm form = new BookForm();
        form.setTitle(title);
        form.setAuthor(author);
        form.setIsbn(isbn);
        form.setPublisher("Editora Teste");
        form.setPublicationYear(2024);
        form.setGenre("Tecnologia");
        form.setDescription("Descrição de teste.");
        form.setTotalPages(totalPages);
        return form;
    }
}