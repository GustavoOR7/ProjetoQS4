package br.edu.qualidade.biblioteca.domain.model;

import br.edu.qualidade.biblioteca.domain.valueobject.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LibraryUserTest {

    @Test
    @DisplayName("Deve criar usuário habilitado com e-mail normalizado e papel ROLE_USER")
    void shouldCreateEnabledUserWithNormalizedEmailAndDefaultRole() {
        LibraryUser user = new LibraryUser(
                " Ana Silva ",
                " ANA@EXAMPLE.COM ",
                " hash-seguro "
        );

        assertThat(user.getName()).isEqualTo("Ana Silva");
        assertThat(user.getEmail()).isEqualTo("ana@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hash-seguro");
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.getRoles()).containsExactly(UserRole.ROLE_USER);
    }

    @Test
    @DisplayName("Deve atualizar perfil normalizando nome e e-mail")
    void shouldUpdateProfileWithNormalizedValues() {
        LibraryUser user = new LibraryUser(
                "Ana",
                "ana@example.com",
                "hash-antigo"
        );

        user.updateProfile(" Maria Souza ", " MARIA@EXAMPLE.COM ");

        assertThat(user.getName()).isEqualTo("Maria Souza");
        assertThat(user.getEmail()).isEqualTo("maria@example.com");
    }

    @Test
    @DisplayName("Deve alterar hash da senha")
    void shouldChangePasswordHash() {
        LibraryUser user = new LibraryUser(
                "Ana",
                "ana@example.com",
                "hash-antigo"
        );

        user.changePasswordHash(" hash-novo ");

        assertThat(user.getPasswordHash()).isEqualTo("hash-novo");
    }

    @Test
    @DisplayName("Deve desabilitar e habilitar usuário")
    void shouldDisableAndEnableUser() {
        LibraryUser user = new LibraryUser(
                "Ana",
                "ana@example.com",
                "hash"
        );

        user.disable();

        assertThat(user.isEnabled()).isFalse();

        user.enable();

        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("Deve conceder e revogar papel mantendo pelo menos um papel")
    void shouldGrantAndRevokeRoleKeepingAtLeastOneRole() {
        LibraryUser user = new LibraryUser(
                "Ana",
                "ana@example.com",
                "hash"
        );

        user.grantRole(UserRole.ROLE_ADMIN);

        assertThat(user.getRoles())
                .containsExactlyInAnyOrder(UserRole.ROLE_USER, UserRole.ROLE_ADMIN);

        user.revokeRole(UserRole.ROLE_ADMIN);

        assertThat(user.getRoles()).containsExactly(UserRole.ROLE_USER);

        user.revokeRole(UserRole.ROLE_USER);

        assertThat(user.getRoles()).containsExactly(UserRole.ROLE_USER);
    }

    @Test
    @DisplayName("Não deve alterar papéis ao conceder ou revogar papel nulo")
    void shouldIgnoreNullRoleChanges() {
        LibraryUser user = new LibraryUser(
                "Ana",
                "ana@example.com",
                "hash"
        );

        user.grantRole(null);
        user.revokeRole(null);

        assertThat(user.getRoles()).containsExactly(UserRole.ROLE_USER);
    }

    @Test
    @DisplayName("Coleção de papéis exposta deve ser imutável")
    void shouldExposeUnmodifiableRoles() {
        LibraryUser user = new LibraryUser(
                "Ana",
                "ana@example.com",
                "hash"
        );

        Set<UserRole> roles = user.getRoles();

        assertThatThrownBy(() -> roles.add(UserRole.ROLE_ADMIN))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}