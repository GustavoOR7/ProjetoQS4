package br.edu.qualidade.biblioteca.web.controller;

import br.edu.qualidade.biblioteca.testsupport.HttpTestSupport;
import io.restassured.filter.cookie.CookieFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HomeControllerIT extends HttpTestSupport {

    @Test
    @DisplayName("GET / deve redirecionar usuário anônimo para login")
    void shouldRedirectAnonymousUserToLogin() {
        given()
                .redirects().follow(false)
                .when()
                .get("/")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("GET / deve redirecionar usuário autenticado para livros")
    void shouldRedirectAuthenticatedUserToBooks() {
        CookieFilter browser = authenticatedBrowser(uniqueEmail(), "Senha123");

        given()
                .filter(browser)
                .redirects().follow(false)
                .when()
                .get("/")
                .then()
                .statusCode(302)
                .header("Location", containsString("/books"));
    }
}