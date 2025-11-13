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

## Integração com Front‑end

- Base URL: `http://localhost:8080`
- Autenticação:
  - `POST /auth/login` com body `{"username":"<user>","senha":"<pass>"}`
  - Resposta: token JWT
  - Use o cabeçalho `Authorization: Bearer <token>` nas chamadas subsequentes
- Endpoints comuns:
  - Usuários: `POST /pessoas` (cadastro público), `GET /pessoas` (FUNCIONARIO/ADMIN)
  - Empréstimos: `POST /emprestimos`, `PUT /emprestimos/{id}/devolver`, `PUT /emprestimos/{id}/renovar`
  - Reservas: `POST /reservas` (USUARIO), `PUT /reservas/{id}/cancelar`, `GET /reservas` (FUNCIONARIO/ADMIN)
  - Livros: `GET /livros` (lista/paginação/filtros conforme controllers)

### Exemplo com fetch (JS)

```js
const API = 'http://localhost:8080';

async function login(username, senha) {
  const r = await fetch(`${API}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, senha })
  });
  if (!r.ok) throw new Error('Login falhou');
  const { token } = await r.json();
  return token;
}

async function listarLivros(token) {
  const r = await fetch(`${API}/livros`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  if (!r.ok) throw new Error('Falha ao listar livros');
  return r.json();
}
```

### Exemplo com axios

```js
import axios from 'axios';
const api = axios.create({ baseURL: 'http://localhost:8080' });

export async function login(username, senha) {
  const { data } = await api.post('/auth/login', { username, senha });
  api.defaults.headers.common.Authorization = `Bearer ${data.token}`;
  return data;
}

export async function criarReserva(usuarioId, livroId) {
  const { data } = await api.post('/reservas', { usuarioId, livroId });
  return data;
}
```

### CORS e Ambientes

- Se o front rodar em outra origem (ex.: `http://localhost:3000`), habilite CORS no back.
- Caso necessário, ajuste a configuração de segurança para permitir o origin do front.
- Sugestão de variável no front: `VITE_API_URL=http://localhost:8080` (ou equivalente) e leia via `import.meta.env.VITE_API_URL`.

### Mensagens e Erros

- Erros de validação: 400 com detalhes por campo.
- Conflitos (409): duplicidade (username/CPF) e FKs (status de empréstimo/reserva). O back retorna mensagens amigáveis.
- Login inválido: 401.

### Fluxos recomendados no front

- Cadastro público → Login → Ações (reservar/emprestar) condicionadas ao papel e slots (≤ 3).
- Reserva com livro disponível gera empréstimo imediato e retorna mensagem de confirmação.
- Devolução transfere automaticamente ao primeiro da fila elegível; caso contrário, livro fica `RESERVADO`.

### Exemplo Angular

- `src/environments/environment.ts`

```ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080'
};
```

- `src/app/services/auth.service.ts`

```ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private http: HttpClient) {}

  login(username: string, senha: string) {
    return this.http.post<any>(`${environment.apiUrl}/auth/login`, { username, senha });
  }

  setToken(token: string) { localStorage.setItem('token', token); }
  getToken() { return localStorage.getItem('token'); }
  logout() { localStorage.removeItem('token'); }
}
```

- `src/app/interceptors/token.interceptor.ts`

```ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  constructor(private auth: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.auth.getToken();
    const authReq = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
    return next.handle(authReq);
  }
}
```

- `src/app/services/reservas.service.ts`

```ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReservasService {
  constructor(private http: HttpClient) {}
  criarReserva(usuarioId: number, livroId: number) {
    return this.http.post<any>(`${environment.apiUrl}/reservas`, { usuarioId, livroId });
  }
  cancelarReserva(id: number) {
    return this.http.put<any>(`${environment.apiUrl}/reservas/${id}/cancelar`, {});
  }
  listar() { return this.http.get<any>(`${environment.apiUrl}/reservas`); }
}
```

- `src/app/services/livros.service.ts`

```ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class LivrosService {
  constructor(private http: HttpClient) {}
  listar() { return this.http.get<any>(`${environment.apiUrl}/livros`); }
}
```

- `src/app/app.module.ts`

```ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppComponent } from './app.component';
import { TokenInterceptor } from './interceptors/token.interceptor';

@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule, HttpClientModule],
  providers: [{ provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }],
  bootstrap: [AppComponent]
})
export class AppModule {}
```

- `src/app/components/login/login.component.ts`

```ts
import { Component } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { ReservasService } from '../../services/reservas.service';
import { LivrosService } from '../../services/livros.service';

@Component({ selector: 'app-login', template: `<button (click)="doLogin()">Login</button>` })
export class LoginComponent {
  constructor(private auth: AuthService, private reservas: ReservasService, private livros: LivrosService) {}

  doLogin() {
    this.auth.login('usuario', 'User@123').subscribe(({ token }) => {
      this.auth.setToken(token);
      this.livros.listar().subscribe(console.log);
      this.reservas.criarReserva(1, 1).subscribe(console.log);
    });
  }
}
```

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
