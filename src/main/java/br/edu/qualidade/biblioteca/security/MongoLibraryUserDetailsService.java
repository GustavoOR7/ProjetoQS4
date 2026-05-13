package br.edu.qualidade.biblioteca.security;

import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import br.edu.qualidade.biblioteca.domain.repository.LibraryUserRepository;
import br.edu.qualidade.biblioteca.domain.valueobject.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MongoLibraryUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(MongoLibraryUserDetailsService.class);

    private final LibraryUserRepository userRepository;

    public MongoLibraryUserDetailsService(LibraryUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        String normalizedEmail = normalizeEmail(email);

        log.debug("Tentando autenticar usuário. emailNormalizado='{}'", normalizedEmail);

        LibraryUser user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado no MongoDB. emailNormalizado='{}'", normalizedEmail);
                    return new UsernameNotFoundException("Usuário não encontrado.");
                });

        log.debug(
                "Usuário encontrado. id={}, email={}, enabled={}, roles={}, passwordHashLength={}, passwordHashPrefix={}",
                user.getId(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles(),
                user.getPasswordHash() == null ? null : user.getPasswordHash().length(),
                safePrefix(user.getPasswordHash())
        );

        String[] authorities = user.getRoles()
                .stream()
                .map(UserRole::name)
                .toArray(String[]::new);

        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(authorities)
                .disabled(!user.isEnabled())
                .build();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String safePrefix(String passwordHash) {
        if (passwordHash == null || passwordHash.length() < 7) {
            return passwordHash;
        }

        return passwordHash.substring(0, 7);
    }
}