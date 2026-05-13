package br.edu.qualidade.biblioteca.web.advice;

import br.edu.qualidade.biblioteca.service.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Instant;

@ControllerAdvice
public class WebExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request,
            Model model
    ) {
        model.addAttribute("status", 404);
        model.addAttribute("error", "Recurso não encontrado");
        model.addAttribute("message", exception.getMessage());
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", Instant.now());

        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(
            Exception exception,
            HttpServletRequest request,
            Model model
    ) {
        model.addAttribute("status", 500);
        model.addAttribute("error", "Erro interno");
        model.addAttribute("message", "Ocorreu um erro inesperado.");
        model.addAttribute("path", request.getRequestURI());
        model.addAttribute("timestamp", Instant.now());

        return "error/error";
    }
}