# RTM — Requirements Traceability Matrix

Projeto: Biblioteca Pessoal  
Disciplina: Qualidade de Software  
Arquitetura: Spring Boot MVC + MongoDB + Spring Security  
Estratégia de teste: Zero Mocks + Testcontainers + HTTP real

---

## Política de testes

Este projeto segue a restrição:

> Zero Mocks.

Não são utilizados:

- Mockito
- PowerMock
- EasyMock
- `@Mock`
- `@MockBean`
- `@InjectMocks`
- `@WithMockUser`

São utilizados:

- MongoDB real com Testcontainers.
- Spring Boot real.
- Spring Security real.
- Repositories reais.
- Services reais.
- Controllers reais.
- Requisições HTTP reais via RestAssured.
- Cobertura via JaCoCo.

---

## Matriz de rastreabilidade

| ID | Requisito funcional | Implementação principal | Testes associados | Tipo de teste | Status |
|---|---|---|---|---|---|
| RF01 | Cadastrar usuário | `AuthController`, `RegistrationService`, `LibraryUserRepository` | `AuthControllerIT.shouldRegisterUserThroughHttp`, `RegistrationServiceIT.shouldRegisterUserWithNormalizedEmailAndEncodedPassword`, `LibraryUserRepositoryIT.shouldSaveAndFindUserByEmail` | Controller, Service, Repository | Coberto |
| RF02 | Validar senha forte no cadastro | `RegistrationService` | `RegistrationServiceIT.shouldRejectWeakPasswords`, `AuthControllerIT.shouldRejectWeakPasswordThroughHttp` | Parametrizado, Controller | Coberto |
| RF03 | Impedir e-mail duplicado | `RegistrationService`, `LibraryUserRepository` | `RegistrationServiceIT.shouldRejectDuplicatedEmail`, `LibraryUserRepositoryIT.shouldRejectDuplicatedEmail` | Service, Repository | Coberto |
| RF04 | Autenticar usuário com sessão | `SecurityConfig`, `MongoLibraryUserDetailsService`, `AuthController` | `AuthControllerIT.shouldLoginRegisteredUserThroughHttp` | Caixa preta HTTP | Coberto |
| RF05 | Bloquear acesso anônimo a rotas protegidas | `SecurityConfig`, `BookController` | `BookControllerIT.shouldRedirectAnonymousUserToLogin`, `HomeControllerIT.shouldRedirectAnonymousUserToLogin` | Caixa preta HTTP | Coberto |
| RF06 | Redirecionar usuário autenticado da raiz para livros | `HomeController` | `HomeControllerIT.shouldRedirectAuthenticatedUserToBooks` | Controller HTTP | Coberto |
| RF07 | Cadastrar livro | `BookController`, `BookService`, `BookRepository` | `BookControllerIT.shouldCreateBookThroughHttp`, `BookServiceIT.shouldCreateBookForOwner`, `BookRepositoryIT.shouldSaveAndFindBookByOwner` | Controller, Service, Repository | Coberto |
| RF08 | Validar dados inválidos de livro | `BookForm`, `BookController`, `Book` | `BookControllerIT.shouldRejectInvalidBookThroughHttp`, `BookWhiteBoxTest.shouldRejectInvalidTotalPages` | Caixa preta, Caixa branca | Coberto |
| RF09 | Listar apenas livros do usuário autenticado | `BookController`, `BookService`, `BookRepository` | `BookControllerIT.shouldListOnlyAuthenticatedUserBooks`, `BookServiceIT.shouldListOnlyOwnerBooks`, `BookRepositoryIT.shouldCountOnlyOwnerBooks` | Controller, Service, Repository | Coberto |
| RF10 | Buscar livros por termo | `BookService`, `BookRepository` | `BookServiceIT.shouldSearchOwnerBooksByTerm`, `BookRepositoryIT.shouldSearchByTerm` | Service, Repository | Coberto |
| RF11 | Visualizar detalhe de livro próprio | `BookController`, `BookService` | `BookControllerIT.shouldCreateBookThroughHttp`, `BookServiceIT.shouldPreventAccessToAnotherUserBook` | Controller, Service | Coberto |
| RF12 | Impedir acesso a livro de outro usuário | `BookService`, `BookController` | `BookControllerIT.shouldReturn404WhenAccessingAnotherUserBook`, `BookServiceIT.shouldPreventAccessToAnotherUserBook`, `BookRepositoryIT.shouldFindBookOnlyForCorrectOwner` | Controller, Service, Repository | Coberto |
| RF13 | Atualizar progresso de leitura | `BookController`, `BookService`, `Book` | `BookControllerIT.shouldUpdateBookProgressThroughHttp`, `BookServiceIT.shouldUpdateBookStatusAccordingToProgress`, `BookWhiteBoxTest.shouldMarkAsReadingWhenProgressIsPartial` | Controller, Parametrizado, Caixa branca | Coberto |
| RF14 | Rejeitar progresso inválido | `BookService`, `Book` | `BookControllerIT.shouldRejectInvalidProgressThroughHttp`, `BookServiceIT.shouldRejectProgressGreaterThanTotalPages`, `BookWhiteBoxTest.shouldRejectCurrentPageGreaterThanTotalPages` | Controller, Service, Caixa branca | Coberto |
| RF15 | Marcar livro como abandonado | `BookController`, `BookService`, `Book` | `BookServiceIT.shouldMarkBookAsAbandoned`, `BookWhiteBoxTest.shouldMarkBookAsAbandoned` | Service, Caixa branca | Coberto |
| RF16 | Retornar livro para quero ler | `Book`, `BookController`, `BookService` | `BookWhiteBoxTest.shouldMarkBookAsToRead` | Caixa branca | Coberto |
| RF17 | Remover livro | `BookController`, `BookService`, `BookRepository` | `BookControllerIT.shouldDeleteBookThroughHttp`, `BookServiceIT.shouldDeleteBook` | Controller, Service | Coberto |
| RF18 | Impedir ISBN duplicado para o mesmo usuário | `BookService`, `BookRepository` | `BookServiceIT.shouldRejectDuplicatedIsbnForSameOwner`, `BookRepositoryIT.shouldRejectDuplicatedIsbnForSameOwner` | Service, Repository | Coberto |
| RF19 | Permitir mesmo ISBN para usuários diferentes | `BookService`, `BookRepository` | `BookServiceIT.shouldAllowSameIsbnForDifferentOwners`, `BookRepositoryIT.shouldAllowSameIsbnForDifferentOwners` | Service, Repository | Coberto |
| RF20 | Manter regras internas do domínio de livro | `Book` | `BookWhiteBoxTest` | Caixa branca | Coberto |
| RF21 | Manter regras internas do domínio de usuário | `LibraryUser` | `LibraryUserTest` | Unitário | Coberto |
| RF22 | Garantir build com cobertura mínima | `pom.xml`, `jacoco-maven-plugin` | `mvn clean verify` | Qualidade/DevOps | Coberto |

---

# Diagramas UML de Sequência

## RF01 — Cadastro de usuário

```mermaid
sequenceDiagram
    actor Visitante
    participant AuthController
    participant RegistrationService
    participant LibraryUserRepository
    participant PasswordEncoder
    participant MongoDB

    Visitante->>AuthController: POST /register
    AuthController->>RegistrationService: register(form)
    RegistrationService->>RegistrationService: normalizar e-mail
    RegistrationService->>RegistrationService: validar senha e confirmação
    RegistrationService->>LibraryUserRepository: existsByEmail(email)
    LibraryUserRepository->>MongoDB: consultar users
    MongoDB-->>LibraryUserRepository: resultado
    RegistrationService->>PasswordEncoder: encode(password)
    PasswordEncoder-->>RegistrationService: passwordHash
    RegistrationService->>LibraryUserRepository: save(LibraryUser)
    LibraryUserRepository->>MongoDB: inserir usuário
    MongoDB-->>LibraryUserRepository: usuário persistido
    LibraryUserRepository-->>RegistrationService: LibraryUser
    RegistrationService-->>AuthController: usuário cadastrado
    AuthController-->>Visitante: redirect /login?registered