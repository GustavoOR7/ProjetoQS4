package br.edu.qualidade.biblioteca.domain.repository;

import br.edu.qualidade.biblioteca.config.MongoAuditingConfig;
import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import br.edu.qualidade.biblioteca.domain.valueobject.UserRole;
import br.edu.qualidade.biblioteca.testsupport.AbstractMongoContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataMongoTest
@Import(MongoAuditingConfig.class)
class LibraryUserRepositoryIT extends AbstractMongoContainerTest {

    @Autowired
    private LibraryUserRepository userRepository;

    @Test
    @DisplayName("Deve salvar usuário real no MongoDB e buscar por e-mail")
    void shouldSaveAndFindUserByEmail() {
        LibraryUser user = new LibraryUser(
                "Ana Silva",
                "ANA@EXAMPLE.COM",
                "$2a$12$abcdefghijklmnopqrstuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu"
        );

        LibraryUser saved = userRepository.save(user);

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getEmail()).isEqualTo("ana@example.com");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getRoles()).containsExactly(UserRole.ROLE_USER);

        assertThat(userRepository.findByEmail("ana@example.com"))
                .isPresent()
                .get()
                .extracting(LibraryUser::getName)
                .isEqualTo("Ana Silva");
    }

    @Test
    @DisplayName("Deve rejeitar e-mail duplicado usando índice único real do MongoDB")
    void shouldRejectDuplicatedEmail() {
        LibraryUser firstUser = new LibraryUser(
                "Ana Silva",
                "ana@example.com",
                "$2a$12$abcdefghijklmnopqrstuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu"
        );

        LibraryUser secondUser = new LibraryUser(
                "Ana Duplicada",
                "ANA@EXAMPLE.COM",
                "$2a$12$abcdefghijklmnopqrstuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu"
        );

        userRepository.save(firstUser);

        assertThatThrownBy(() -> userRepository.save(secondUser))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("Deve retornar falso quando e-mail não existir")
    void shouldReturnFalseWhenEmailDoesNotExist() {
        assertThat(userRepository.existsByEmail("inexistente@example.com")).isFalse();
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando e-mail existir")
    void shouldReturnTrueWhenEmailExists() {
        userRepository.save(new LibraryUser(
                "Carlos Souza",
                "carlos@example.com",
                "$2a$12$abcdefghijklmnopqrstuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu"
        ));

        assertThat(userRepository.existsByEmail("carlos@example.com")).isTrue();
    }
}