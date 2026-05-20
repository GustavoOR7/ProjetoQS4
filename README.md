# Biblioteca Pessoal

Aplicação web MVC para cadastro e gerenciamento de livros de uma biblioteca pessoal, com autenticação de usuários, controle de sessão, persistência em MongoDB e foco em qualidade de software.

O projeto foi desenvolvido para a disciplina de **Qualidade de Software** com foco em:

- Testabilidade.
- Cobertura de código.
- Integração contínua.
- Segurança básica de sessão.
- Persistência real em MongoDB.
- Proibição total de frameworks de mock.

---

## Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data MongoDB
- Thymeleaf
- MongoDB
- Maven
- JUnit 5
- Testcontainers
- RestAssured
- JaCoCo
- SonarQube / SonarCloud
- GitHub Actions

---

## Regra crítica de qualidade: Zero Mocks

Este projeto segue a regra:

> Não usar Mockito, PowerMock, EasyMock, `@Mock`, `@MockBean`, `@InjectMocks` ou similares.

As integrações são testadas com cenários reais e isolados:

- MongoDB real via Testcontainers.
- Spring Boot real.
- Spring Security real.
- HTTP real via RestAssured.
- Controllers reais.
- Services reais.
- Repositories reais.

O `pom.xml` possui regra Maven Enforcer para bloquear dependências de mock.

---

## Funcionalidades

### Usuários

- Cadastro de usuário.
- Validação de e-mail.
- Validação de senha forte.
- Login com Spring Security.
- Logout.
- Gerenciamento seguro de sessão.

### Livros

- Cadastro de livro.
- Listagem de livros do usuário autenticado.
- Busca por título, autor, gênero ou ISBN.
- Visualização de detalhes.
- Edição de dados bibliográficos.
- Atualização de progresso de leitura.
- Marcação automática de status:
  - `TO_READ`
  - `READING`
  - `FINISHED`
  - `ABANDONED`
- Remoção de livro.
- Isolamento de dados por usuário.
- Restrição de ISBN duplicado dentro da biblioteca do mesmo usuário.

---

## Pré-requisitos

Para rodar localmente:

- Java 21
- Maven 3.9+
- Docker
- MongoDB local ou Docker

Para rodar os testes:

- Docker ativo

O Docker é necessário porque os testes de integração sobem um MongoDB real via Testcontainers.

---

## Como rodar a aplicação localmente

Suba um MongoDB local:

```bash
docker run --name biblioteca-mongo -p 27017:27017 -d mongo:7.0