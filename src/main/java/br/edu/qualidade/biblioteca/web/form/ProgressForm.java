package br.edu.qualidade.biblioteca.web.form;

import jakarta.validation.constraints.PositiveOrZero;

public class ProgressForm {

    @PositiveOrZero(message = "A página atual não pode ser negativa.")
    private Integer currentPage;

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
}