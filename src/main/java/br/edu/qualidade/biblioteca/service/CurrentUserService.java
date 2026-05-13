package br.edu.qualidade.biblioteca.service;

import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import br.edu.qualidade.biblioteca.domain.repository.LibraryUserRepository;
import br.edu.qualidade.biblioteca.service.exception.ResourceNotFoundException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CurrentUserService {

    private final LibraryUserRepository userRepository;

    public CurrentUserService(LibraryUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LibraryUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResourceNotFoundException("Usuário autenticado não encontrado.");
        }

        String email = authentication.getName().trim().toLowerCase(Locale.ROOT);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado."));
    }
}