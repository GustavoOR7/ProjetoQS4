package br.edu.qualidade.biblioteca.domain.model;

import br.edu.qualidade.biblioteca.domain.valueobject.BookStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookWhiteBoxTest {

    @Test
    @DisplayName("Deve criar livro com status inicial TO_READ e página atual zero")
    void shouldCreateBookWithInitialStatus() {
        Book book = new Book(
                "user-1",
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                "Prentice Hall",
                2008,
                "Software",
                "Livro sobre código limpo.",
                464
        );

        assertThat(book.getStatus()).isEqualTo(BookStatus.TO_READ);
        assertThat(book.getCurrentPage()).isZero();
        assertThat(book.getFinishedAt()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100, 463})
    @DisplayName("Deve marcar livro como READING quando progresso for parcial")
    void shouldMarkAsReadingWhenProgressIsPartial(int currentPage) {
        Book book = validBook();

        book.updateProgress(currentPage);

        assertThat(book.getStatus()).isEqualTo(BookStatus.READING);
        assertThat(book.getCurrentPage()).isEqualTo(currentPage);
        assertThat(book.getFinishedAt()).isNull();
    }

    @Test
    @DisplayName("Deve marcar livro como FINISHED quando página atual for igual ao total")
    void shouldMarkAsFinishedWhenCurrentPageEqualsTotalPages() {
        Book book = validBook();

        book.updateProgress(464);

        assertThat(book.getStatus()).isEqualTo(BookStatus.FINISHED);
        assertThat(book.getCurrentPage()).isEqualTo(464);
        assertThat(book.getFinishedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve voltar para TO_READ quando progresso for zero")
    void shouldReturnToReadWhenProgressIsZero() {
        Book book = validBook();
        book.updateProgress(200);

        book.updateProgress(0);

        assertThat(book.getStatus()).isEqualTo(BookStatus.TO_READ);
        assertThat(book.getCurrentPage()).isZero();
        assertThat(book.getFinishedAt()).isNull();
    }

    @Test
    @DisplayName("Deve rejeitar página atual maior que total de páginas")
    void shouldRejectCurrentPageGreaterThanTotalPages() {
        Book book = validBook();

        assertThatThrownBy(() -> book.updateProgress(465))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("A página atual não pode ser maior");
    }

    @Test
    @DisplayName("Deve rejeitar página atual negativa")
    void shouldRejectNegativeCurrentPage() {
        Book book = validBook();

        assertThatThrownBy(() -> book.updateProgress(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ser negativa");
    }

    @Test
    @DisplayName("Deve rejeitar total de páginas menor ou igual a zero")
    void shouldRejectInvalidTotalPages() {
        assertThatThrownBy(() -> new Book(
                "user-1",
                "Livro inválido",
                "Autor",
                null,
                null,
                2024,
                null,
                null,
                0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("total de páginas");
    }

    @Test
    @DisplayName("Deve marcar livro como abandonado")
    void shouldMarkBookAsAbandoned() {
        Book book = validBook();
        book.updateProgress(100);

        book.markAsAbandoned();

        assertThat(book.getStatus()).isEqualTo(BookStatus.ABANDONED);
        assertThat(book.getFinishedAt()).isNull();
    }

    @Test
@DisplayName("Deve atualizar dados bibliográficos normalizando campos opcionais vazios")
void shouldUpdateBibliographicDataAndNormalizeOptionalBlankFields() {
    Book book = validBook();

    book.updateBibliographicData(
            " Refactoring ",
            " Martin Fowler ",
            " ",
            " ",
            1999,
            " ",
            " ",
            300
    );

    assertThat(book.getTitle()).isEqualTo("Refactoring");
    assertThat(book.getAuthor()).isEqualTo("Martin Fowler");
    assertThat(book.getIsbn()).isNull();
    assertThat(book.getPublisher()).isNull();
    assertThat(book.getPublicationYear()).isEqualTo(1999);
    assertThat(book.getGenre()).isNull();
    assertThat(book.getDescription()).isNull();
    assertThat(book.getTotalPages()).isEqualTo(300);
}

@Test
@DisplayName("Deve indicar se livro pertence ao usuário informado")
void shouldCheckBookOwnership() {
    Book book = validBook();

    assertThat(book.belongsTo("user-1")).isTrue();
    assertThat(book.belongsTo("outro-user")).isFalse();
    assertThat(book.belongsTo(null)).isFalse();
}

@Test
@DisplayName("Deve marcar livro como quero ler")
void shouldMarkBookAsToRead() {
    Book book = validBook();
    book.updateProgress(100);

    book.markAsToRead();

    assertThat(book.getCurrentPage()).isZero();
    assertThat(book.getStatus()).isEqualTo(BookStatus.TO_READ);
    assertThat(book.getFinishedAt()).isNull();
}

    private Book validBook() {
        return new Book(
                "user-1",
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                "Prentice Hall",
                2008,
                "Software",
                "Livro sobre código limpo.",
                464
        );
    }
}