package br.edu.qualidade.biblioteca.web.form;

import br.edu.qualidade.biblioteca.domain.model.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookFormTest {

    @Test
    @DisplayName("Deve criar formulário a partir de um livro existente")
    void shouldCreateFormFromBook() {
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

        BookForm form = BookForm.from(book);

        assertThat(form.getTitle()).isEqualTo("Clean Code");
        assertThat(form.getAuthor()).isEqualTo("Robert C. Martin");
        assertThat(form.getIsbn()).isEqualTo("9780132350884");
        assertThat(form.getPublisher()).isEqualTo("Prentice Hall");
        assertThat(form.getPublicationYear()).isEqualTo(2008);
        assertThat(form.getGenre()).isEqualTo("Software");
        assertThat(form.getDescription()).isEqualTo("Livro sobre código limpo.");
        assertThat(form.getTotalPages()).isEqualTo(464);
    }

    @Test
    @DisplayName("Deve preencher formulário usando setters")
    void shouldFillFormUsingSetters() {
        BookForm form = new BookForm();

        form.setTitle("Refactoring");
        form.setAuthor("Martin Fowler");
        form.setIsbn("9780201485677");
        form.setPublisher("Addison-Wesley");
        form.setPublicationYear(1999);
        form.setGenre("Software");
        form.setDescription("Livro sobre refatoração.");
        form.setTotalPages(300);

        assertThat(form.getTitle()).isEqualTo("Refactoring");
        assertThat(form.getAuthor()).isEqualTo("Martin Fowler");
        assertThat(form.getIsbn()).isEqualTo("9780201485677");
        assertThat(form.getPublisher()).isEqualTo("Addison-Wesley");
        assertThat(form.getPublicationYear()).isEqualTo(1999);
        assertThat(form.getGenre()).isEqualTo("Software");
        assertThat(form.getDescription()).isEqualTo("Livro sobre refatoração.");
        assertThat(form.getTotalPages()).isEqualTo(300);
    }
}