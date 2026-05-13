package br.edu.qualidade.biblioteca.web.form;

import br.edu.qualidade.biblioteca.domain.model.Book;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class BookForm {

    @NotBlank(message = "O título é obrigatório.")
    @Size(min = 2, max = 160, message = "O título deve ter entre 2 e 160 caracteres.")
    private String title;

    @NotBlank(message = "O autor é obrigatório.")
    @Size(min = 2, max = 120, message = "O autor deve ter entre 2 e 120 caracteres.")
    private String author;

    @Size(max = 20, message = "O ISBN deve ter no máximo 20 caracteres.")
    private String isbn;

    @Size(max = 120, message = "A editora deve ter no máximo 120 caracteres.")
    private String publisher;

    @Min(value = 1450, message = "O ano de publicação deve ser maior ou igual a 1450.")
    @Max(value = 2100, message = "O ano de publicação deve ser menor ou igual a 2100.")
    private Integer publicationYear;

    @Size(max = 80, message = "O gênero deve ter no máximo 80 caracteres.")
    private String genre;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
    private String description;

    @Positive(message = "O total de páginas deve ser maior que zero.")
    private Integer totalPages;

    public static BookForm from(Book book) {
        BookForm form = new BookForm();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setIsbn(book.getIsbn());
        form.setPublisher(book.getPublisher());
        form.setPublicationYear(book.getPublicationYear());
        form.setGenre(book.getGenre());
        form.setDescription(book.getDescription());
        form.setTotalPages(book.getTotalPages());
        return form;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
}