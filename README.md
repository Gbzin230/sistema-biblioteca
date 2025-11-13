# Sistema Biblioteca — Backend + MySQL (Windows)

## Pré‑requisitos

- Java 17 (JDK)
- Maven 3.8+
- MySQL Server 8.0+ (Workbench opcional)
- PowerShell (Windows) ou CMD

## Instalação

- Java: instale e confirme `java -version`
- Maven: instale e confirme `mvn -version`
- MySQL: instale o servidor local (porta 3306 por padrão) e confirme que o serviço está ativo

## Banco de Dados (MySQL)

1) Criar banco e usuário dedicados (execute no MySQL):

```sql
CREATE DATABASE IF NOT EXISTS biblioteca_db;
CREATE USER 'biblioteca'@'localhost' IDENTIFIED BY 'sua_senha_aqui';
GRANT ALL PRIVILEGES ON biblioteca_db.* TO 'biblioteca'@'localhost';
FLUSH PRIVILEGES;
```

2) Aplicar schema e compatibilizações do backend (uma única vez):

- Execute, em ordem:
  - `src/main/resources/schema-amigo.sql`
  - `src/main/resources/schema-mysql.sql`

Use MySQL Workbench (File → Open SQL Script → Run) ou `mysql.exe`:

```powershell
Get-Content -Raw "c:\…\sistema-biblioteca\src\main\resources\schema-amigo.sql" | & "C:\Program Files\MySQL\MySQL Server 8.x\bin\mysql.exe" -u biblioteca -psua_senha_aqui
Get-Content -Raw "c:\…\sistema-biblioteca\src\main\resources\schema-mysql.sql" | & "C:\Program Files\MySQL\MySQL Server 8.x\bin\mysql.exe" -u biblioteca -psua_senha_aqui
```

3) Semear dados iniciais (uma única vez):

- Execute `src/main/resources/seed-mysql.sql`:

```powershell
Get-Content -Raw "c:\…\sistema-biblioteca\src\main\resources\seed-mysql.sql" | & "C:\Program Files\MySQL\MySQL Server 8.x\bin\mysql.exe" -u biblioteca -psua_senha_aqui
```

Observação: senhas em texto serão criptografadas automaticamente no primeiro boot do backend.

## Executar o Backend (MySQL, porta 8080)

No diretório do projeto (`sistema-biblioteca`), configure variáveis e rode:

```powershell
 
$env:SPRING_DATASOURCE_USERNAME = "biblioteca"
$env:SPRING_DATASOURCE_PASSWORD = "sua_senha_aqui"
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://localhost:3306/biblioteca_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:SERVER_PORT = "8080"

mvn -q spring-boot:run
```

- Para persistir variáveis (nova sessão), use `setx`:

```powershell
setx SPRING_DATASOURCE_USERNAME biblioteca
setx SPRING_DATASOURCE_PASSWORD sua_senha_aqui
setx SPRING_DATASOURCE_URL "jdbc:mysql://localhost:3306/biblioteca_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
setx SERVER_PORT 8080
```

## Testes (Swagger)

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

## Fluxos Implementados

- Empréstimo: valida usuário ativo e slots (≤ 3), altera livro para `EMPRESTADO`.
- Devolução: encerra empréstimo e transfere automaticamente para o primeiro da fila de reservas elegível; 
  se houver fila e ninguém puder pegar, livro fica `RESERVADO`; senão `DISPONIVEL`.
- Reserva: se o livro estiver `DISPONIVEL`, a reserva realiza empréstimo imediato e retorna mensagem de confirmação; 
  caso contrário, cria fila e marca `RESERVADO` quando apropriado.

## Solução de Problemas

- Access denied: confirme usuário/senha e execute `GRANT` conforme acima.
- Unknown database: crie `biblioteca_db` ou use `createDatabaseIfNotExist=true` na URL.
- FK de status (409): garanta inserts em `TB_STATUS_EMPRESTIMO` e `TB_STATUS_RESERVA`.
- Porta 8080 ocupada: libere processo ou defina `SERVER_PORT` para outra porta.
- Reexecução de schema: execute os scripts apenas uma vez; mantenha `spring.sql.init.mode=never`.

## Observações

- Não coloque credenciais reais de e‑mail no `application.properties`. Configure `spring.mail.*` apenas em ambientes apropriados.
- O `ModelMapper` converte `dtNascimento` entre `String` (entidade) e `LocalDate` (DTOs) automaticamente.
- Limite de slots por usuário: 3 (soma de empréstimos ATIVOS + reservas ATIVAS).
