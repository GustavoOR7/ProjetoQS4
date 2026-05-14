package br.edu.qualidade.biblioteca.web.controller;

import br.edu.qualidade.biblioteca.domain.repository.BookRepository;
import br.edu.qualidade.biblioteca.testsupport.HttpTestSupport;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerIT extends HttpTestSupport {

    @Autowired
    private BookRepository bookRepository;

    @Test
    @DisplayName("GET /books sem autenticação deve redirecionar para login")
    void shouldRedirectAnonymousUserToLogin() {
        given()
                .redirects().follow(false)
                .when()
                .get("/books")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login"));
    }

    @Test
    @DisplayName("Usuário autenticado deve conseguir cadastrar livro via HTTP real")
    void shouldCreateBookThroughHttp() {
        CookieFilter browser = authenticatedBrowser(uniqueEmail(), "Senha123");

        String bookPath = createBookThroughHttp(
                browser,
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                464
        );

        assertThat(bookRepository.count()).isEqualTo(1);

        given()
                .filter(browser)
                .when()
                .get(bookPath)
                .then()
                .statusCode(200)
                .body(containsString("Clean Code"))
                .body(containsString("Robert C. Martin"))
                .body(containsString("9780132350884"));
    }

    @Test
    @DisplayName("POST /books/new deve rejeitar livro inválido")
    void shouldRejectInvalidBookThroughHttp() {
        CookieFilter browser = authenticatedBrowser(uniqueEmail(), "Senha123");
        String csrf = getCsrfToken("/books/new", browser);

        given()
                .filter(browser)
                .contentType(ContentType.URLENC)
                .formParam("_csrf", csrf)
                .formParam("title", "")
                .formParam("author", "")
                .formParam("isbn", "isbn-invalido")
                .formParam("publisher", "Editora")
                .formParam("publicationYear", 1200)
                .formParam("genre", "Tecnologia")
                .formParam("description", "Descricao")
                .formParam("totalPages", -1)
                .when()
                .post("/books/new")
                .then()
                .statusCode(200)
                .body(containsString("O título é obrigatório"))
                .body(containsString("O autor é obrigatório"))
                .body(containsString("maior ou igual a 1450"))
                .body(containsString("maior que zero"));
    }

    @Test
    @DisplayName("Usuário autenticado deve listar somente seus próprios livros")
    void shouldListOnlyAuthenticatedUserBooks() {
        CookieFilter browserA = authenticatedBrowser(uniqueEmail(), "Senha123");
        CookieFilter browserB = authenticatedBrowser(uniqueEmail(), "Senha123");

        createBookThroughHttp(browserA, "Livro do usuario A", "Autor A", "isbn-a", 200);
        createBookThroughHttp(browserB, "Livro do usuario B", "Autor B", "isbn-b", 300);

        given()
                .filter(browserA)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body(containsString("Livro do usuario A"))
                .body(org.hamcrest.Matchers.not(containsString("Livro do usuario B")));
    }

    @Test
    @DisplayName("Usuário não deve acessar detalhe de livro de outro usuário")
    void shouldReturn404WhenAccessingAnotherUserBook() {
        CookieFilter browserA = authenticatedBrowser(uniqueEmail(), "Senha123");
        CookieFilter browserB = authenticatedBrowser(uniqueEmail(), "Senha123");

        String privateBookPath = createBookThroughHttp(
                browserA,
                "Livro privado",
                "Autor Privado",
                "isbn-privado",
                200
        );

        given()
                .filter(browserB)
                .when()
                .get(privateBookPath)
                .then()
                .statusCode(404)
                .body(containsString("Recurso não encontrado"));
    }

    @Test
    @DisplayName("Usuário deve atualizar progresso do livro via HTTP")
    void shouldUpdateBookProgressThroughHttp() {
        CookieFilter browser = authenticatedBrowser(uniqueEmail(), "Senha123");

        String bookPath = createBookThroughHttp(
                browser,
                "Refactoring",
                "Martin Fowler",
                "9780201485677",
                300
        );

        String csrf = getCsrfToken(bookPath, browser);

        given()
                .filter(browser)
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("_csrf", csrf)
                .formParam("currentPage", 300)
                .when()
                .post(bookPath + "/progress")
                .then()
                .statusCode(302)
                .header("Location", containsString(bookPath));

        given()
                .filter(browser)
                .when()
                .get(bookPath)
                .then()
                .statusCode(200)
                .body(containsString("FINISHED"))
                .body(containsString("300"));
    }

    @Test
    @DisplayName("Usuário deve receber mensagem de erro ao informar progresso inválido")
    void shouldRejectInvalidProgressThroughHttp() {
        CookieFilter browser = authenticatedBrowser(uniqueEmail(), "Senha123");

        String bookPath = createBookThroughHttp(
                browser,
                "Livro com progresso invalido",
                "Autor",
                "isbn-prog-invalid",
                100
        );

        String csrf = getCsrfToken(bookPath, browser);

        given()
                .filter(browser)
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("_csrf", csrf)
                .formParam("currentPage", 101)
                .when()
                .post(bookPath + "/progress")
                .then()
                .statusCode(302)
                .header("Location", containsString(bookPath));

        given()
                .filter(browser)
                .when()
                .get(bookPath)
                .then()
                .statusCode(200)
                .body(containsString("A página atual não pode ser maior que o total de páginas"));
    }

    @Test
    @DisplayName("Usuário deve remover livro via HTTP")
    void shouldDeleteBookThroughHttp() {
        CookieFilter browser = authenticatedBrowser(uniqueEmail(), "Senha123");

        String bookPath = createBookThroughHttp(
                browser,
                "Livro removivel",
                "Autor",
                "isbn-delete-http",
                100
        );

        assertThat(bookRepository.count()).isEqualTo(1);

        String csrf = getCsrfToken(bookPath, browser);

        given()
                .filter(browser)
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("_csrf", csrf)
                .when()
                .post(bookPath + "/delete")
                .then()
                .statusCode(302)
                .header("Location", containsString("/books"));

        assertThat(bookRepository.count()).isZero();
    }
}