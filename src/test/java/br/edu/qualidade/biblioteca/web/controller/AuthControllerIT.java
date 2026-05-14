package br.edu.qualidade.biblioteca.web.controller;

import br.edu.qualidade.biblioteca.domain.repository.LibraryUserRepository;
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
class AuthControllerIT extends HttpTestSupport {

    @Autowired
    private LibraryUserRepository userRepository;

    @Test
    @DisplayName("GET /login deve renderizar página de login")
    void shouldRenderLoginPage() {
        given()
                .when()
                .get("/login")
                .then()
                .statusCode(200)
                .body(containsString("Entrar"))
                .body(containsString("username"))
                .body(containsString("password"));
    }

    @Test
    @DisplayName("GET /register deve renderizar página de cadastro")
    void shouldRenderRegistrationPage() {
        given()
                .when()
                .get("/register")
                .then()
                .statusCode(200)
                .body(containsString("Criar conta"))
                .body(containsString("name"))
                .body(containsString("email"))
                .body(containsString("password"))
                .body(containsString("_csrf"));
    }

    @Test
    @DisplayName("POST /register deve cadastrar usuário real no MongoDB")
    void shouldRegisterUserThroughHttp() {
        CookieFilter browser = newBrowser();
        String email = uniqueEmail();

        registerUserThroughHttp(browser, "Ana Silva", email, "Senha123");

        assertThat(userRepository.findByEmail(email)).isPresent();
    }

    @Test
    @DisplayName("POST /register deve rejeitar senha fraca")
    void shouldRejectWeakPasswordThroughHttp() {
        CookieFilter browser = newBrowser();
        String csrf = getCsrfToken("/register", browser);

        given()
                .filter(browser)
                .contentType(ContentType.URLENC)
                .formParam("_csrf", csrf)
                .formParam("name", "Ana Silva")
                .formParam("email", uniqueEmail())
                .formParam("password", "senhafraca")
                .formParam("confirmPassword", "senhafraca")
                .when()
                .post("/register")
                .then()
                .statusCode(200)
                .body(containsString("Criar conta"))
                .body(containsString("A senha deve conter pelo menos uma letra maiúscula"));
    }

    @Test
    @DisplayName("POST /login deve autenticar usuário cadastrado e redirecionar para /books")
    void shouldLoginRegisteredUserThroughHttp() {
        CookieFilter browser = newBrowser();
        String email = uniqueEmail();
        String password = "Senha123";

        registerUserThroughHttp(browser, "Ana Silva", email, password);
        loginThroughHttp(browser, email, password);

        given()
                .filter(browser)
                .when()
                .get("/books")
                .then()
                .statusCode(200)
                .body(containsString("Meus livros"));
    }

    @Test
    @DisplayName("POST /login deve redirecionar para erro quando credenciais forem inválidas")
    void shouldRejectInvalidCredentials() {
        CookieFilter browser = newBrowser();
        String csrf = getCsrfToken("/login", browser);

        given()
                .filter(browser)
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("_csrf", csrf)
                .formParam("username", "naoexiste@example.com")
                .formParam("password", "SenhaErrada123")
                .when()
                .post("/login")
                .then()
                .statusCode(302)
                .header("Location", containsString("/login?error"));
    }
}