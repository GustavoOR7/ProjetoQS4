package br.edu.qualidade.biblioteca.testsupport;

import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class HttpTestSupport extends AbstractMongoContainerTest {

    private static final Pattern CSRF_PATTERN = Pattern.compile(
            "name=\"_csrf\"[^>]*value=\"([^\"]+)\"|value=\"([^\"]+)\"[^>]*name=\"_csrf\""
    );

    @LocalServerPort
    protected int port;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    protected CookieFilter newBrowser() {
        return new CookieFilter();
    }

    protected String getCsrfToken(String path, CookieFilter browser) {
        Response response = given()
                .filter(browser)
                .when()
                .get(path)
                .then()
                .extract()
                .response();

        assertThat(response.statusCode())
                .as("GET " + path + " precisa retornar 200 para extrair CSRF")
                .isEqualTo(200);

        return extractCsrfToken(response.asString());
    }

    protected String extractCsrfToken(String html) {
        Matcher matcher = CSRF_PATTERN.matcher(html);

        if (!matcher.find()) {
            throw new AssertionError("Token CSRF não encontrado no HTML.");
        }

        if (matcher.group(1) != null) {
            return matcher.group(1);
        }

        return matcher.group(2);
    }

    protected String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    protected void registerUserThroughHttp(
            CookieFilter browser,
            String name,
            String email,
            String password
    ) {
        String csrf = getCsrfToken("/register", browser);

        given()
                .filter(browser)
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("_csrf", csrf)
                .formParam("name", name)
                .formParam("email", email)
                .formParam("password", password)
                .formParam("confirmPassword", password)
                .when()
                .post("/register")
                .then()
                .statusCode(302)
                .header("Location", org.hamcrest.Matchers.containsString("/login?registered"));
    }

    protected void loginThroughHttp(
            CookieFilter browser,
            String email,
            String password
    ) {
        String csrf = getCsrfToken("/login", browser);

        given()
                .filter(browser)
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("_csrf", csrf)
                .formParam("username", email)
                .formParam("password", password)
                .when()
                .post("/login")
                .then()
                .statusCode(302)
                .header("Location", org.hamcrest.Matchers.containsString("/books"));
    }

    protected CookieFilter authenticatedBrowser(String email, String password) {
        CookieFilter browser = newBrowser();

        registerUserThroughHttp(browser, "Usuário Teste", email, password);
        loginThroughHttp(browser, email, password);

        return browser;
    }

    protected String createBookThroughHttp(
            CookieFilter browser,
            String title,
            String author,
            String isbn,
            Integer totalPages
    ) {
        String csrf = getCsrfToken("/books/new", browser);

        Response response = given()
                .filter(browser)
                .redirects().follow(false)
                .contentType(ContentType.URLENC)
                .formParam("_csrf", csrf)
                .formParam("title", title)
                .formParam("author", author)
                .formParam("isbn", isbn)
                .formParam("publisher", "Editora Teste")
                .formParam("publicationYear", 2024)
                .formParam("genre", "Tecnologia")
                .formParam("description", "Livro criado por teste automatizado.")
                .formParam("totalPages", totalPages)
                .when()
                .post("/books/new")
                .then()
                .statusCode(302)
                .header("Location", org.hamcrest.Matchers.containsString("/books/"))
                .extract()
                .response();

        return pathFromLocation(response.header("Location"));
    }

    protected String pathFromLocation(String location) {
        if (location == null || location.isBlank()) {
            throw new AssertionError("Header Location não informado.");
        }

        if (location.startsWith("http://") || location.startsWith("https://")) {
            return URI.create(location).getPath();
        }

        return location;
    }
}