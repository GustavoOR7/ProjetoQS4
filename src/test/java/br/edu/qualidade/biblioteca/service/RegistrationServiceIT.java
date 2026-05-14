package br.edu.qualidade.biblioteca.service;

import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import br.edu.qualidade.biblioteca.domain.repository.LibraryUserRepository;
import br.edu.qualidade.biblioteca.service.exception.BusinessRuleException;
import br.edu.qualidade.biblioteca.testsupport.AbstractMongoContainerTest;
import br.edu.qualidade.biblioteca.web.form.RegistrationForm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class RegistrationServiceIT extends AbstractMongoContainerTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private LibraryUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Deve cadastrar usuário com e-mail normalizado e senha com hash BCrypt")
    void shouldRegisterUserWithNormalizedEmailAndEncodedPassword() {
        RegistrationForm form = registrationForm(
                "Ana Silva",
                "ANA@EXAMPLE.COM",
                "Senha123",
                "Senha123"
        );

        LibraryUser registeredUser = registrationService.register(form);

        assertThat(registeredUser.getId()).isNotBlank();
        assertThat(registeredUser.getEmail()).isEqualTo("ana@example.com");
        assertThat(registeredUser.getPasswordHash()).isNotEqualTo("Senha123");
        assertThat(registeredUser.getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("Senha123", registeredUser.getPasswordHash())).isTrue();

        assertThat(userRepository.findByEmail("ana@example.com")).isPresent();
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com e-mail duplicado")
    void shouldRejectDuplicatedEmail() {
        registrationService.register(registrationForm(
                "Ana Silva",
                "ana@example.com",
                "Senha123",
                "Senha123"
        ));

        RegistrationForm duplicatedForm = registrationForm(
                "Ana Duplicada",
                "ANA@EXAMPLE.COM",
                "Senha123",
                "Senha123"
        );

        assertThatThrownBy(() -> registrationService.register(duplicatedForm))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Já existe");
    }

    @Test
    @DisplayName("Deve rejeitar senha e confirmação diferentes")
    void shouldRejectDifferentPasswordConfirmation() {
        RegistrationForm form = registrationForm(
                "Bruno Lima",
                "bruno@example.com",
                "Senha123",
                "OutraSenha123"
        );

        assertThatThrownBy(() -> registrationService.register(form))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("devem ser iguais");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "senhafraca",
            "SENHAFRACA",
            "12345678",
            "SenhaSemNumero",
            "senha1234"
    })
    @DisplayName("Deve rejeitar senhas fracas em múltiplos cenários")
    void shouldRejectWeakPasswords(String weakPassword) {
        RegistrationForm form = registrationForm(
                "Usuário Teste",
                "teste-" + weakPassword.hashCode() + "@example.com",
                weakPassword,
                weakPassword
        );

        assertThatThrownBy(() -> registrationService.register(form))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("maiúscula");
    }

    private RegistrationForm registrationForm(
            String name,
            String email,
            String password,
            String confirmPassword
    ) {
        RegistrationForm form = new RegistrationForm();
        form.setName(name);
        form.setEmail(email);
        form.setPassword(password);
        form.setConfirmPassword(confirmPassword);
        return form;
    }
}