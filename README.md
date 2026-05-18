🛡️ Network Scanner API
API REST para monitoramento e análise de vulnerabilidades de rede, construída com Java + Spring Boot. A aplicação orquestra scans automatizados com Nmap e Nuclei dentro de containers Docker isolados via playbooks Ansible, e utiliza uma LLM (OpenRouter) para gerar relatórios inteligentes sobre os resultados.

# Disclaimer
**Atenção:** Esta ferramenta foi desenvolvida exclusivamente para fins educacionais, de pesquisa e para uso em ambientes de teste controlados e devidamente autorizados. O uso em redes, sistemas ou aplicações sem permissão explícita do proprietário é ilegal e pode resultar em sanções civis e criminais.

O desenvolvedor e os colaboradores **não se responsabilizam** por qualquer uso indevido, danos, prejuízos ou consequências legais decorrentes da utilização deste software. Sempre obtenha autorização formal antes de realizar qualquer tipo de varredura, análise ou teste de segurança em sistemas de terceiros.

Ao utilizar este projeto, você concorda em seguir todas as leis e regulamentos aplicáveis à sua jurisdição e assume total responsabilidade por suas ações. Utilize apenas em ambientes de testes autorizados ou pessoais!

# Tutorial de testar Who is safe?

## Requisitos

* Docker e Docker Compose
* Java 17+
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

Para verificar se o banco e a imagem do scanner estão disponíveis:

```shell
docker ps -a              # postgres UP, scanner Exited (normal)
docker images | grep scanner-image   # imagem deve aparecer
```

---

### 3. Compilar e rodar a aplicação

```shell
chmod +x mvnw
export $(cat .env | xargs) && sudo ./mvnw spring-boot:run
```

A aplicação vai iniciar na porta **8080**.

---

### 4. Acessar o frontend

Abra o arquivo `index.html` na raiz do projeto diretamente no navegador:

```shell
xdg-open index.html
```

Ou dê dois cliques no arquivo pelo gerenciador de arquivos.
