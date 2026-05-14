package br.edu.qualidade.biblioteca.testsupport;

import br.edu.qualidade.biblioteca.domain.model.Book;
import br.edu.qualidade.biblioteca.domain.model.LibraryUser;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractMongoContainerTest {

    private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:7.0");

    protected static final MongoDBContainer MONGO_CONTAINER =
            new MongoDBContainer(MONGO_IMAGE);

    static {
        MONGO_CONTAINER.start();
    }

    @Autowired
    protected MongoTemplate mongoTemplate;

    @DynamicPropertySource
    static void registerMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_CONTAINER::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");

        registry.add("server.servlet.session.cookie.secure", () -> "false");
        registry.add("server.servlet.session.cookie.same-site", () -> "lax");
    }

    @BeforeEach
    void cleanDatabase() {
        mongoTemplate.remove(new Query(), Book.class);
        mongoTemplate.remove(new Query(), LibraryUser.class);
    }
}