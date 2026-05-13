package br.edu.qualidade.biblioteca.service;

import br.edu.qualidade.biblioteca.domain.model.Book;
import br.edu.qualidade.biblioteca.domain.repository.BookRepository;
import br.edu.qualidade.biblioteca.service.exception.BusinessRuleException;
import br.edu.qualidade.biblioteca.service.exception.ResourceNotFoundException;
import br.edu.qualidade.biblioteca.web.form.BookForm;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> listBooks(String ownerUserId, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return bookRepository.findByOwnerUserIdOrderByCreatedAtDesc(ownerUserId);
        }

        String escapedSearchTerm = Pattern.quote(searchTerm.trim());

        return bookRepository.searchByOwnerUserIdAndTerm(
                ownerUserId,
                escapedSearchTerm,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    @Transactional(readOnly = true)
    public Book getBookForOwner(String bookId, String ownerUserId) {
        return bookRepository.findByIdAndOwnerUserId(bookId, ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Livro não encontrado."));
    }

    @Transactional
    public Book createBook(String ownerUserId, BookForm form) {
        validateUniqueIsbnForOwner(ownerUserId, form.getIsbn(), null);

        Book book = new Book(
                ownerUserId,
                form.getTitle(),
                form.getAuthor(),
                form.getIsbn(),
                form.getPublisher(),
                form.getPublicationYear(),
                form.getGenre(),
                form.getDescription(),
                form.getTotalPages()
        );

        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(String bookId, String ownerUserId, BookForm form) {
        Book book = getBookForOwner(bookId, ownerUserId);

        validateUniqueIsbnForOwner(ownerUserId, form.getIsbn(), book.getId());

        book.updateBibliographicData(
                form.getTitle(),
                form.getAuthor(),
                form.getIsbn(),
                form.getPublisher(),
                form.getPublicationYear(),
                form.getGenre(),
                form.getDescription(),
                form.getTotalPages()
        );

        return bookRepository.save(book);
    }

    @Transactional
    public Book updateProgress(String bookId, String ownerUserId, Integer currentPage) {
        Book book = getBookForOwner(bookId, ownerUserId);

        try {
            book.updateProgress(currentPage);
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException(exception.getMessage());
        }

        return bookRepository.save(book);
    }

    @Transactional
    public Book markAsAbandoned(String bookId, String ownerUserId) {
        Book book = getBookForOwner(bookId, ownerUserId);
        book.markAsAbandoned();
        return bookRepository.save(book);
    }

    @Transactional
    public Book markAsToRead(String bookId, String ownerUserId) {
        Book book = getBookForOwner(bookId, ownerUserId);
        book.markAsToRead();
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(String bookId, String ownerUserId) {
        Book book = getBookForOwner(bookId, ownerUserId);
        bookRepository.delete(book);
    }

    private void validateUniqueIsbnForOwner(
            String ownerUserId,
            String rawIsbn,
            String currentBookId
    ) {
        if (rawIsbn == null || rawIsbn.isBlank()) {
            return;
        }

        String isbn = rawIsbn.trim();

        bookRepository.findByOwnerUserIdAndIsbn(ownerUserId, isbn)
                .filter(existingBook -> currentBookId == null || !existingBook.getId().equals(currentBookId))
                .ifPresent(existingBook -> {
                    throw new BusinessRuleException("Já existe um livro com este ISBN na sua biblioteca.");
                });
    }
}