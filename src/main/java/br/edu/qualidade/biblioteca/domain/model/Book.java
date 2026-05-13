package br.edu.qualidade.biblioteca.domain.model;

import br.edu.qualidade.biblioteca.domain.valueobject.BookStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

@Document(collection = "books")
@CompoundIndexes({
        @CompoundIndex(
                name = "idx_books_owner_title_author",
                def = "{ 'ownerUserId': 1, 'title': 1, 'author': 1 }"
        ),
        @CompoundIndex(
                name = "idx_books_owner_isbn_unique_sparse",
                def = "{ 'ownerUserId': 1, 'isbn': 1 }",
                unique = true,
                sparse = true
        )
})
public class Book {

    @Id
    private String id;

    @NotBlank(message = "O livro precisa estar associado a um usuário.")
    private String ownerUserId;

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

    @PositiveOrZero(message = "A página atual não pode ser negativa.")
    private Integer currentPage;

    private BookStatus status;

    private Instant finishedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;

    protected Book() {
        // Necessário para o Spring Data MongoDB.
    }

    public Book(
            String ownerUserId,
            String title,
            String author,
            String isbn,
            String publisher,
            Integer publicationYear,
            String genre,
            String description,
            Integer totalPages
    ) {
        this.ownerUserId = normalizeRequired(ownerUserId);
        this.title = normalizeRequired(title);
        this.author = normalizeRequired(author);
        this.isbn = normalizeOptional(isbn);
        this.publisher = normalizeOptional(publisher);
        this.publicationYear = publicationYear;
        this.genre = normalizeOptional(genre);
        this.description = normalizeOptional(description);
        this.totalPages = totalPages;
        this.currentPage = 0;
        this.status = BookStatus.TO_READ;

        validatePageInvariant(this.currentPage, this.totalPages);
    }

    public void updateBibliographicData(
            String title,
            String author,
            String isbn,
            String publisher,
            Integer publicationYear,
            String genre,
            String description,
            Integer totalPages
    ) {
        this.title = normalizeRequired(title);
        this.author = normalizeRequired(author);
        this.isbn = normalizeOptional(isbn);
        this.publisher = normalizeOptional(publisher);
        this.publicationYear = publicationYear;
        this.genre = normalizeOptional(genre);
        this.description = normalizeOptional(description);
        this.totalPages = totalPages;

        validatePageInvariant(this.currentPage, this.totalPages);
    }

    public void updateProgress(Integer newCurrentPage) {
        int safePage = Objects.requireNonNullElse(newCurrentPage, 0);

        validatePageInvariant(safePage, this.totalPages);

        this.currentPage = safePage;

        if (safePage == 0) {
            this.status = BookStatus.TO_READ;
            this.finishedAt = null;
            return;
        }

        if (this.totalPages != null && safePage == this.totalPages) {
            this.status = BookStatus.FINISHED;
            this.finishedAt = Instant.now();
            return;
        }

        this.status = BookStatus.READING;
        this.finishedAt = null;
    }

    public void markAsAbandoned() {
        this.status = BookStatus.ABANDONED;
        this.finishedAt = null;
    }

    public void markAsToRead() {
        this.currentPage = 0;
        this.status = BookStatus.TO_READ;
        this.finishedAt = null;
    }

    public boolean belongsTo(String userId) {
        return this.ownerUserId != null && this.ownerUserId.equals(userId);
    }

    private static void validatePageInvariant(Integer currentPage, Integer totalPages) {
        if (currentPage != null && currentPage < 0) {
            throw new IllegalArgumentException("A página atual não pode ser negativa.");
        }

        if (totalPages != null && totalPages <= 0) {
            throw new IllegalArgumentException("O total de páginas deve ser maior que zero.");
        }

        if (currentPage != null && totalPages != null && currentPage > totalPages) {
            throw new IllegalArgumentException("A página atual não pode ser maior que o total de páginas.");
        }
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public String getId() {
        return id;
    }

    public String getOwnerUserId() {
        return ownerUserId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public String getGenre() {
        return genre;
    }

    public String getDescription() {
        return description;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public BookStatus getStatus() {
        return status;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}