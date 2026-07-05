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

---

# Ambientes de Teste de Vulnerabilidades

O projeto inclui ambientes de teste com aplicações vulneráveis para simular cenários reais de análise:

- **DVWA** 
- **Juice Shop**

Esses ambientes são definidos no arquivo `docker-compose.vulnlab.yml`.

### 5. Subindo os containers de teste (opcional)

Execute o comando abaixo na raiz do projeto:

```shell
docker compose -f docker-compose.vulnlab.yml up -d
```

Os serviços estarão disponíveis em:

- DVWA:        http://localhost:8081
- Juice Shop:  http://localhost:8082

#### Verificando se os containers estão UP

Para garantir que os containers das aplicações vulneráveis estão rodando:

```shell
docker ps
```

Você deve ver as linhas referentes a `dvwa` e `juice-shop` com STATUS `Up`.
Com isso ao cadastrar o seu ip local como alvo, o scan deverá reconhecer que as portas desses serviços criados estão abertas e indicará no relatório. 

### Derrubando os containers de teste

Para parar e remover os containers de teste:

```shell
docker compose -f docker-compose.vulnlab.yml down
```

---

# Monitoramento Contínuo

Além do scan manual, a API permite configurar **auditorias programadas e
automatizadas por alvo**. Um scheduler interno verifica periodicamente quais
alvos têm monitoramento ativo e cujo intervalo configurado já venceu, roda o
scan (reaproveitando o mesmo pipeline do scan manual) e, se encontrar algo
relevante (portas novas ou vulnerabilidades), gera um relatório via LLM e
registra uma notificação para a equipe.

**Pontos flexíveis** (configuráveis por alvo, na aba **MONITORAMENTO** do
frontend):

* **Ferramenta**: `NMAP` ou `NUCLEI`
* **Execução do scan**: `baremetal` ou `docker`
* **Canal de notificação**: `log` (console do servidor) ou `webhook`
  (Slack/Discord — configure `monitoramento.webhook.url` no
  `application.properties`, ou `WEBHOOK_URL` no `.env`)
* **Intervalo**: em minutos, por alvo

## Endpoints

| Método | Rota                              | Descrição                                   |
|--------|-----------------------------------|----------------------------------------------|
| GET    | `/Monitoramento/get`              | Lista as configurações de monitoramento       |
| POST   | `/Monitoramento/post/{alvoId}`    | Cria/atualiza a configuração de um alvo       |
| PUT    | `/Monitoramento/toggle/{alvoId}/{ativo}` | Ativa/desativa rapidamente             |
| POST   | `/Monitoramento/executar/{alvoId}`| Roda a auditoria imediatamente (útil p/ testes)|
| DELETE | `/Monitoramento/delete/{alvoId}`  | Remove a configuração                         |
| GET    | `/Notificacao/get`                | Lista os alertas gerados                      |
| PUT    | `/Notificacao/lida/{id}`          | Marca um alerta como lido                     |
| DELETE | `/Notificacao/delete/{id}`        | Remove um alerta                              |

O intervalo de checagem do scheduler (não confundir com o intervalo de cada
auditoria) é configurável em `monitoramento.scheduler.fixedDelay` (padrão:
60000 ms).

---