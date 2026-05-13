package br.edu.qualidade.biblioteca.domain.repository;

import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LibraryUserRepository extends MongoRepository<LibraryUser, String> {

    Optional<LibraryUser> findByEmail(String email);

    boolean existsByEmail(String email);
}