package br.edu.qualidade.biblioteca.service;

import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import br.edu.qualidade.biblioteca.domain.repository.LibraryUserRepository;
import br.edu.qualidade.biblioteca.service.exception.BusinessRuleException;
import br.edu.qualidade.biblioteca.web.form.RegistrationForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class RegistrationService {

    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"
    );

    private final LibraryUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            LibraryUserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public LibraryUser register(RegistrationForm form) {
        String normalizedEmail = normalizeEmail(form.getEmail());

        validatePasswordConfirmation(form.getPassword(), form.getConfirmPassword());
        validatePasswordStrength(form.getPassword());
        validateEmailAvailability(normalizedEmail);

        String passwordHash = passwordEncoder.encode(form.getPassword());

        LibraryUser user = new LibraryUser(
                form.getName(),
                normalizedEmail,
                passwordHash
        );

        return userRepository.save(user);
    }

    private void validateEmailAvailability(String normalizedEmail) {
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessRuleException("Já existe um usuário cadastrado com este e-mail.");
        }
    }

    private void validatePasswordConfirmation(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new BusinessRuleException("A senha e a confirmação de senha devem ser iguais.");
        }
    }

    private void validatePasswordStrength(String password) {
        if (password == null || !STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessRuleException(
                    "A senha deve conter pelo menos uma letra maiúscula, uma letra minúscula e um número."
            );
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}