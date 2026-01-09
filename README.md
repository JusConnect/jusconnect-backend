# JusConnect Backend

Este projeto é um backend Spring Boot para o sistema JusConnect.

## Pré-requisitos
- Java 21
- Gradle (wrapper incluso)
- PostgreSQL


## Configuração do Banco de Dados

### Usando Docker
1. Certifique-se de que Docker e Docker Compose estão instalados.
2. Na raiz do projeto, execute:
	```sh
	docker-compose up -d postgres
	```
	Isso irá baixar a imagem do PostgreSQL, criar o container `jusconnect-postgres` e expor a porta 5432.
3. O banco estará disponível em `localhost:5432` com:
	- Banco: `jusconnect`
	- Usuário: `jusconnect_user`
	- Senha: `senha`

### Sem Docker (manual)
1. Instale o PostgreSQL localmente.
2. Crie o banco e usuário conforme o arquivo `src/main/resources/application.properties`.
3. Ajuste as credenciais se necessário.

## Rodando o Projeto

### 1. Build do projeto

No terminal, na raiz do projeto:
```sh
./gradlew build
```

### 2. Executando a aplicação

## Perfis de Execução

### Perfil Padrão (PostgreSQL)
Inicia com PostgreSQL (padrão):
```sh
./gradlew bootRun
```

### Perfil H2 (Desenvolvimento/Testes)
Inicia com H2 para desenvolvimento:
```sh
./gradlew bootRun --args='--spring.profiles.active=h2'
```

### Perfil Produção
Inicia com configurações de produção:
```sh
./gradlew bootRun --args='--spring.profiles.active=prod'
```

A aplicação estará disponível em: http://localhost:8080

## Observações
- As tabelas do banco são criadas automaticamente ao rodar a aplicação.
- Para ambiente de desenvolvimento, o banco pode ser limpo/deletado para recriação automática das tabelas.

---
Dúvidas ou problemas? Abra uma issue ou entre em contato com o time de desenvolvimento.
