package br.edu.qualidade.biblioteca.domain.model;

import br.edu.qualidade.biblioteca.domain.valueobject.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Document(collection = "users")
@CompoundIndex(
        name = "idx_users_email_unique",
        def = "{ 'email': 1 }",
        unique = true
)
public class LibraryUser {

    @Id
    private String id;

    @NotBlank(message = "O nome é obrigatório.")
    @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres.")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail deve ser válido.")
    @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.")
    private String email;

    @NotBlank(message = "O hash da senha é obrigatório.")
    private String passwordHash;

    @NotEmpty(message = "O usuário deve possuir ao menos um papel.")
    private Set<UserRole> roles = new HashSet<>();

    private boolean enabled;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    private Long version;

    protected LibraryUser() {
        // Necessário para o Spring Data MongoDB.
    }

    public LibraryUser(String name, String email, String passwordHash) {
        this.name = normalizeRequired(name);
        this.email = normalizeEmail(email);
        this.passwordHash = normalizeRequired(passwordHash);
        this.roles = new HashSet<>();
        this.roles.add(UserRole.ROLE_USER);
        this.enabled = true;
    }

    public void updateProfile(String name, String email) {
        this.name = normalizeRequired(name);
        this.email = normalizeEmail(email);
    }

    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = normalizeRequired(newPasswordHash);
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public void grantRole(UserRole role) {
        if (role != null) {
            this.roles.add(role);
        }
    }

    public void revokeRole(UserRole role) {
        if (role != null && this.roles.size() > 1) {
            this.roles.remove(role);
        }
    }

    private static String normalizeRequired(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.trim();
    }

    private static String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Set<UserRole> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean isEnabled() {
        return enabled;
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