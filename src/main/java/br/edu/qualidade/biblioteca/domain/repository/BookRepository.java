package br.edu.qualidade.biblioteca.domain.repository;

import br.edu.qualidade.biblioteca.domain.model.Book;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends MongoRepository<Book, String> {

    List<Book> findByOwnerUserIdOrderByCreatedAtDesc(String ownerUserId);

    Optional<Book> findByIdAndOwnerUserId(String id, String ownerUserId);

    Optional<Book> findByOwnerUserIdAndIsbn(String ownerUserId, String isbn);

    boolean existsByOwnerUserIdAndIsbn(String ownerUserId, String isbn);

    long countByOwnerUserId(String ownerUserId);

    @Query("""
            {
              'ownerUserId': ?0,
              '$or': [
                { 'title': { $regex: ?1, $options: 'i' } },
                { 'author': { $regex: ?1, $options: 'i' } },
                { 'genre': { $regex: ?1, $options: 'i' } },
                { 'isbn': { $regex: ?1, $options: 'i' } }
              ]
            }
            """)
    List<Book> searchByOwnerUserIdAndTerm(String ownerUserId, String escapedSearchTerm, Sort sort);
}