package br.edu.qualidade.biblioteca.web.advice;

import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import br.edu.qualidade.biblioteca.service.CurrentUserService;
import br.edu.qualidade.biblioteca.service.exception.ResourceNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final CurrentUserService currentUserService;

    public GlobalModelAttributes(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @ModelAttribute
    public void addCurrentUser(Model model) {
        try {
            LibraryUser user = currentUserService.getAuthenticatedUser();
            model.addAttribute("currentUserName", user.getName());
            model.addAttribute("currentUserEmail", user.getEmail());
        } catch (ResourceNotFoundException ignored) {
            model.addAttribute("currentUserName", null);
            model.addAttribute("currentUserEmail", null);
        }
    }
}