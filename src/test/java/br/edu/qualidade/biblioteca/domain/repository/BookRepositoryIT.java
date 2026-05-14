package br.edu.qualidade.biblioteca.domain.repository;

import br.edu.qualidade.biblioteca.config.MongoAuditingConfig;
import br.edu.qualidade.biblioteca.domain.model.Book;
import br.edu.qualidade.biblioteca.testsupport.AbstractMongoContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@Import(MongoAuditingConfig.class)
class BookRepositoryIT extends AbstractMongoContainerTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("Deve salvar livro real no MongoDB e buscar por dono")
    void shouldSaveAndFindBookByOwner() {
        Book book = validBook("user-1", "Clean Code", "Robert C. Martin", "9780132350884");

        Book saved = bookRepository.save(book);

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        List<Book> books = bookRepository.findByOwnerUserIdOrderByCreatedAtDesc("user-1");

        assertThat(books)
                .hasSize(1)
                .first()
                .extracting(Book::getTitle)
                .isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("Deve isolar livros por usuário")
    void shouldFindBookOnlyForCorrectOwner() {
        Book book = bookRepository.save(
                validBook("owner-1", "Domain-Driven Design", "Eric Evans", "9780321125217")
        );

        assertThat(bookRepository.findByIdAndOwnerUserId(book.getId(), "owner-1"))
                .isPresent();

        assertThat(bookRepository.findByIdAndOwnerUserId(book.getId(), "owner-2"))
                .isEmpty();
    }

    @Test
    @DisplayName("Deve permitir mesmo ISBN para usuários diferentes")
    void shouldAllowSameIsbnForDifferentOwners() {
        bookRepository.save(validBook("owner-1", "Livro A", "Autor A", "isbn-compartilhado"));
        bookRepository.save(validBook("owner-2", "Livro B", "Autor B", "isbn-compartilhado"));

        assertThat(bookRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve rejeitar ISBN duplicado para o mesmo usuário usando índice único real")
    void shouldRejectDuplicatedIsbnForSameOwner() {
        bookRepository.save(validBook("owner-1", "Livro A", "Autor A", "isbn-duplicado"));

        Book duplicated = validBook("owner-1", "Livro B", "Autor B", "isbn-duplicado");

        assertThatThrownBy(() -> bookRepository.save(duplicated))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("Deve buscar por título, autor, gênero ou ISBN")
    void shouldSearchByTerm() {
        bookRepository.save(validBook("owner-1", "Clean Code", "Robert C. Martin", "9780132350884"));
        bookRepository.save(validBook("owner-1", "Refactoring", "Martin Fowler", "9780201485677"));
        bookRepository.save(validBook("owner-2", "Clean Architecture", "Robert C. Martin", "9780134494166"));

            List<Book> result = bookRepository.searchByOwnerUserIdAndTerm(
            "owner-1",
            "martin",
            Sort.by(Sort.Direction.DESC, "createdAt")
    );

        assertThat(result)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Clean Code", "Refactoring");
    }

    @Test
    @DisplayName("Deve contar livros somente do usuário informado")
    void shouldCountOnlyOwnerBooks() {
        bookRepository.save(validBook("owner-1", "Livro A", "Autor A", "isbn-a"));
        bookRepository.save(validBook("owner-1", "Livro B", "Autor B", "isbn-b"));
        bookRepository.save(validBook("owner-2", "Livro C", "Autor C", "isbn-c"));

        assertThat(bookRepository.countByOwnerUserId("owner-1")).isEqualTo(2);
        assertThat(bookRepository.countByOwnerUserId("owner-2")).isEqualTo(1);
    }

    private Book validBook(String ownerUserId, String title, String author, String isbn) {
        return new Book(
                ownerUserId,
                title,
                author,
                isbn,
                "Editora Teste",
                2024,
                "Tecnologia",
                "Descrição de teste",
                300
        );
    }
}