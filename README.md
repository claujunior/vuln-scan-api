🛡️ Network Scanner API
API REST para monitoramento e análise de vulnerabilidades de rede, construída com Java + Spring Boot. A aplicação orquestra scans automatizados com Nmap e Nuclei dentro de containers Docker isolados via playbooks Ansible, e utiliza uma LLM (OpenRouter) para gerar relatórios inteligentes sobre os resultados.

# Tutorial de testar Who is safe?

## Requisitos

* Docker e Docker Compose
* Java 17+
* Postman
* IntelliJ (Opcional)

## Obtendo a chave da API do OpenRouter

O projeto usa o OpenRouter para acessar modelos de IA. Para obter sua chave:

1. Acesse [openrouter.ai](https://openrouter.ai) e crie uma conta (pode usar e-mail, Google ou GitHub)
2. Após fazer login, clique em **Keys** no menu de navegação
3. Clique em **Create Key**, dê um nome para a chave e confirme
4. **Copie a chave imediatamente** — o OpenRouter só a exibe uma vez. Se perder, precisará gerar uma nova
5. Cole a chave no campo `chaveApi` do arquivo `.env` (veja o passo 1 abaixo)

> O OpenRouter oferece modelos gratuitos com limite de 200 requisições/dia, suficiente para testes.

---

## Passo a passo

### 1. Criar o arquivo `.env`

Na raiz do projeto, crie um arquivo chamado `.env` com o seguinte conteúdo:

```env
DB_PASSWORD=123
JWT_SECRET=sua-chave-secreta-longa-aqui
chaveApi=sua-chave-da-openrouter-aqui
```

> `DB_PASSWORD` deve ser `123` para bater com o `docker-compose.yml`.  
> `JWT_SECRET` pode ser qualquer string longa.  
> `chaveApi` é a sua chave da API do OpenRouter.

---

### 2. Subir o banco e construir a imagem do scanner

O backend executa cada scan via `docker run --rm scanner-image ansible-playbook ...`.
A imagem `scanner-image` (definida em `infra/Dockerfile`, contém Ansible + NMAP + Nuclei)
é construída automaticamente junto com o `--build`:

```shell
docker compose up -d --build
```

O serviço `scanner` inicia, executa um `echo` e termina — o container fica "exited",
mas a **imagem permanece disponível** no Docker para os scans em runtime.

Verificar:

```shell
docker ps -a              # postgres UP, scanner Exited (normal)
docker images | grep scanner-image   # imagem deve aparecer
```

---

### 4. Compilar e rodar a aplicação

```shell
chmod +x mvnw
export $(cat .env | xargs) && sudo ./mvnw spring-boot:run
```

A aplicação vai iniciar na porta **8080**.

---

### 5. Registrar um usuário (Postman)

**POST** `http://localhost:8080/auth/register`

- Em **Body → raw → JSON**:

```json
{
  "login": "seu_email@exemplo.com",
  "senha": "sua_senha",
  "cpf": "12345678900"
}
```

Resposta esperada: `Resgistrado com sucesso`

---

### 6. Fazer login e obter o token (Postman)

**POST** `http://localhost:8080/auth/login`

- Em **Body → raw → JSON**:

```json
{
  "login": "seu_email@exemplo.com",
  "senha": "sua_senha"
}
```

A resposta será o **token JWT**. Copie esse token.

---

### 7. Usar o token nas requisições

Em qualquer outra requisição no Postman, vá em:

**Authorization → Bearer Token** → cole o token recebido no login.

---

### 8. Acessar o frontend

Abra o arquivo `index.html` na raiz do projeto diretamente no navegador:

```shell
xdg-open index.html
```

Ou dê dois cliques no arquivo pelo gerenciador de arquivos.
