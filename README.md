# Sample Service

Backend do projeto **Sample**, desenvolvido em **Java 25** com **Spring Boot 4**.

O `sample-service` concentra as APIs, regras de negócio, persistência, migrations e testes automatizados do backend.

O primeiro domínio funcional do projeto é o **Cadastro Hierárquico de Unidades Organizacionais**.

---

## Tecnologias

Principais tecnologias utilizadas:

- Java 25;
- Spring Boot 4;
- Maven;
- Spring Data JPA;
- Flyway;
- SQLite para desenvolvimento local;
- PostgreSQL para homologação e produção;
- Testcontainers;
- OpenAPI / Swagger;
- JaCoCo;
- OWASP Dependency-Check;
- SonarQube.

---

## Responsabilidades

Este repositório é responsável por:

- APIs REST;
- regras de negócio;
- validações;
- persistência;
- migrations da aplicação;
- integração com SQLite e PostgreSQL;
- testes unitários e de integração;
- testes com PostgreSQL utilizando Testcontainers;
- documentação OpenAPI;
- análise de cobertura;
- análise de qualidade e segurança das dependências.

---

## Localização no workspace

No Windows:

```text
C:\Projetos\sample\sample-service
```

Dentro do Dev Container:

```text
/workspaces/sample/sample-service
```

O diretório:

```text
C:\Projetos\sample
```

é apenas o workspace agregador dos repositórios do projeto.

---

## Ambiente de desenvolvimento

O desenvolvimento deve ser realizado preferencialmente pelo **VS Code Dev Container** configurado no repositório:

```text
sample-dev-environment
```

Dentro do Dev Container:

```bash
cd /workspaces/sample/sample-service
```

Verifique Java e Maven:

```bash
java -version
mvn -version
```

---

## Perfis de execução

O backend possui perfis separados para os bancos utilizados no projeto.

### SQLite local

Configuração:

```text
application-local-sqlite.yaml
```

Executar:

```bash
SPRING_PROFILES_ACTIVE=local-sqlite mvn spring-boot:run
```

O banco SQLite utilizado no desenvolvimento local é armazenado fora do repositório.

No Windows:

```text
C:\Dados\sample\sqlite\sample.db
```

Dentro do container:

```text
/data/sqlite/sample.db
```

### PostgreSQL local

Configuração:

```text
application-local-postgres.yaml
```

O PostgreSQL é utilizado por meio do ambiente Docker e seus dados permanecem em volumes nomeados do Docker.

Credenciais e senhas não devem ser armazenadas no código-fonte.

---

## Executar testes

Para executar os testes:

```bash
mvn test
```

Para realizar uma compilação limpa antes dos testes:

```bash
mvn clean test
```

Os testes de integração podem utilizar PostgreSQL real por meio de **Testcontainers**.

---

## Executar a aplicação

Com SQLite:

```bash
SPRING_PROFILES_ACTIVE=local-sqlite mvn spring-boot:run
```

Por padrão, a aplicação estará disponível em:

```text
http://localhost:8080
```

---

## Swagger / OpenAPI

Com a aplicação em execução, a documentação interativa da API pode ser acessada em:

```text
http://localhost:8080/swagger-ui/index.html
```

O contrato OpenAPI pode ser consultado em:

```text
http://localhost:8080/v3/api-docs
```

A ausência de um endpoint para:

```text
http://localhost:8080/
```

é permitida. O backend não precisa fornecer uma página inicial.

---

## Qualidade

O projeto utiliza um profile Maven específico para as verificações de qualidade.

Antes de um commit relevante, execute:

```bash
mvn clean verify -Pquality \
  -DdataDirectory=/odc-data
```

Esse processo contempla as verificações configuradas no profile `quality`, incluindo o **OWASP Dependency-Check**.

> Executar apenas `mvn test` ou `mvn verify` sem `-Pquality` não executa necessariamente todas as verificações de segurança configuradas no projeto.

---

## SonarQube

Para executar análise completa com SonarQube:

```bash
mvn clean verify -Pquality \
  -DdataDirectory=/odc-data \
  sonar:sonar \
  -Dsonar.projectKey=sample-service \
  -Dsonar.host.url=http://host.docker.internal:9000
```

A autenticação com o SonarQube deve utilizar credencial fornecida fora do código-fonte.

Tokens não devem ser armazenados:

- no `pom.xml`;
- no Git;
- no README;
- em scripts versionados.

---

## Cobertura de testes

A cobertura de código é coletada utilizando **JaCoCo**.

O relatório é produzido durante as execuções Maven configuradas para cobertura e pode ser utilizado pelo SonarQube durante a análise do projeto.

A existência de cobertura não substitui a necessidade de testes relevantes para regras de negócio e critérios de aceite.

---

## Segurança de dependências

O projeto utiliza **OWASP Dependency-Check** para identificar vulnerabilidades conhecidas em dependências.

A execução deve ocorrer pelo profile:

```text
quality
```

Exemplo:

```bash
mvn clean verify -Pquality \
  -DdataDirectory=/odc-data
```

Supressões de vulnerabilidades somente devem ser utilizadas quando houver justificativa técnica documentada.

Não devem ser criadas supressões apenas para permitir que o build fique verde.

---

## Configuração local e segredos

O arquivo:

```text
.env
```

pertence ao ambiente local do `sample-service` e **não deve ser versionado**.

O `.gitignore` deve garantir sua exclusão:

```gitignore
.env
```

Quando houver necessidade de documentar variáveis configuráveis, utilize valores de exemplo sem incluir credenciais reais.

Nunca versione:

- senhas;
- tokens;
- chaves privadas;
- credenciais AWS;
- secrets;
- arquivos locais de banco;
- dados de execução;
- logs com informações sensíveis.

---

## Git

O desenvolvimento deve ocorrer em branches de trabalho.

Padrões adotados:

```text
feature/*
release/*
hotfix/*
```

O fluxo esperado é:

```text
feature/*
    ↓
commit
    ↓
push
    ↓
Pull Request
    ↓
revisão
    ↓
merge
    ↓
develop
    ↓
criação de release/x.y.z
    ↓
estabilização / testes
    ↓
Merge Request
    ↓
main
```

A `main` não deve ser utilizada como branch normal de desenvolvimento.

---

## Commits

Os commits devem utilizar mensagens objetivas.

Exemplos:

```text
feat: add organizational unit creation
fix: correct organizational unit validation
test: add hierarchy validation tests
docs: update API documentation
refactor: simplify organizational unit service
chore: update project configuration
```

Principais tipos:

| Tipo | Uso |
|---|---|
| `feat` | nova funcionalidade |
| `fix` | correção de defeito |
| `test` | criação ou alteração de testes |
| `docs` | documentação |
| `refactor` | refatoração sem alteração funcional |
| `chore` | manutenção ou configuração do projeto |

---

## Primeiro domínio funcional

A primeira funcionalidade do Sample é o:

```text
Cadastro Hierárquico de Unidades Organizacionais
```

Sua implementação deve seguir o planejamento aprovado existente em:

```text
sample-governanca/planejamentos/
```

O planejamento, as normas e as skills aplicáveis são fontes normativas do desenvolvimento.


---

## Repositórios relacionados

| Repositório | Responsabilidade |
|---|---|
| `sample-app` | aplicação web Angular |
| `sample-mobile` | aplicação mobile |

---

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.0/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.0/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.1.0/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.1.0/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Validation](https://docs.spring.io/spring-boot/4.1.0/reference/io/validation.html)
* [Spring Security](https://docs.spring.io/spring-boot/4.1.0/reference/web/spring-security.html)
* [OAuth2 Resource Server](https://docs.spring.io/spring-boot/4.1.0/reference/web/spring-security.html#web.security.oauth2.server)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/4.1.0/reference/actuator/index.html)
* [Flyway Migration](https://docs.spring.io/spring-boot/4.1.0/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.1.0/reference/using/devtools.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)


## Licença

Este repositório é privado.

Nenhuma licença open source é concedida automaticamente pela ausência de um arquivo `LICENSE`.

A definição de eventual licença deverá ser uma decisão explícita do projeto.
