# Splitty API

## 📖 Descrição

API REST para divisão de despesas em grupo, permitindo gerenciar grupos, membros, despesas e pagamentos de forma colaborativa. Utiliza processamento assíncrono com filas para criação de pagamentos e envio de notificações.

## ✨ Funcionalidades

- **Gerenciamento de Usuários**: Cadastro, autenticação e atualização de perfil
- **Gerenciamento de Grupos**: Criação e administração de grupos de despesas
- **Gerenciamento de Membros**: Adicionar e remover membros dos grupos com diferentes roles (OWNER/MEMBER)
- **Despesas**: Cadastro de despesas com divisão automática entre membros ativos
- **Pagamentos**: Processamento assíncrono de pagamentos via filas (RabbitMQ)
- **Notificações**: Sistema de notificações de pagamentos
- **Dead Letter Queues**: Recuperação de mensagens com falha para retry manual
- **Múltiplas Moedas**: Suporte a diferentes moedas por grupo (BRL, USD, EUR, etc.)

## 🛠️ Tecnologias

- **Kotlin** 1.9.25
- **Spring Boot** 3.5.6
  - Spring Data JPA
  - Spring Web
  - Spring AMQP (RabbitMQ)
- **PostgreSQL** 16 - Banco de dados
- **RabbitMQ** 3.13 - Sistema de mensageria
- **Flyway** - Migrações de banco de dados
- **Gradle** - Gerenciamento de dependências
- **Docker** + **Docker Compose** - Containerização

## 🚀 Como Executar

### 1. Pré-requisitos

- Java 21
- Docker e Docker Compose

### 2. Subir os serviços (PostgreSQL + RabbitMQ)

```bash
docker-compose up -d
```

Isso irá iniciar:
- **PostgreSQL** na porta `5432`
- **RabbitMQ** na porta `5672` (AMQP)
- **RabbitMQ Management UI** na porta `15672`

### 3. Executar a aplicação

**Modo Completo (API + Consumers):**
```bash
./gradlew bootRun --args='--spring.profiles.active=api,consumer'
```

**Apenas API (sem processar filas):**
```bash
./gradlew bootRun --args='--spring.profiles.active=api'
```

**Apenas Consumers (sem API REST):**
```bash
./gradlew bootRun --args='--spring.profiles.active=consumer'
```

### 4. Acessar serviços

- **API**: `http://localhost:8080/api`
- **RabbitMQ Management**: `http://localhost:15672`
  - Usuário: `splitty`
  - Senha: `splitty123`

## 📊 Arquitetura de Mensageria

### Fluxo de Processamento

```
1. POST /api/expenses
   ↓ (síncrono)
2. Expense criada no banco
   ↓ (assíncrono - 1 mensagem por membro)
3. Mensagens publicadas → expense.payment.create
   ↓
4. PaymentCreationConsumer processa cada mensagem
   ↓
5. Payment criado no banco para cada membro
   ↓
6. Mensagem publicada → payment.notification
   ↓
7. PaymentNotificationConsumer processa
   ↓
8. Notificação enviada (logs por enquanto)
```

### Filas

#### Filas Principais
- `expense.payment.create` - Criação de pagamentos individuais
- `payment.notification` - Notificações de pagamentos

#### Dead Letter Queues (DLQ)
- `expense.payment.create.dlq` - Pagamentos que falharam após 3 tentativas
- `payment.notification.dlq` - Notificações que falharam após 3 tentativas

### Exchanges

- `expense.exchange` - Topic exchange para eventos de despesas
- `notification.exchange` - Topic exchange para notificações
- `dlx.exchange` - Direct exchange para dead letters

### Configuração de Retry

- **Tentativas**: 3
- **Intervalo inicial**: 3 segundos
- **Multiplicador**: 2 (3s → 6s → 12s)

## 📁 Estrutura do Projeto

```
src/main/kotlin/com/splitty/splittyapi/
├── users/              # Gerenciamento de usuários
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── controller/
│   └── dto/
├── groups/             # Gerenciamento de grupos
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── controller/
│   ├── dto/
│   └── mapper/
├── expenses/           # Gerenciamento de despesas
│   ├── entity/
│   ├── repository/
│   ├── service/
│   ├── controller/
│   ├── dto/
│   └── mapper/
├── payments/           # Gerenciamento de pagamentos
│   └── service/
├── messaging/          # Sistema de mensageria (RabbitMQ)
│   ├── config/        # Configuração de filas e exchanges
│   ├── producer/      # Publicadores de mensagens
│   ├── consumer/      # Consumidores de mensagens
│   └── dto/           # DTOs de mensagens
└── config/            # Configurações gerais
```

## 🔑 Principais Endpoints

### Usuários
- `POST /api/users` - Cadastrar usuário
- `GET /api/users/{code}` - Buscar usuário por código
- `PUT /api/users/{code}` - Atualizar usuário
- `DELETE /api/users/{code}` - Desabilitar usuário
- `POST /api/users/login` - Login

### Grupos
- `POST /api/groups` - Criar grupo
- `GET /api/groups/{code}` - Buscar grupo
- `PUT /api/groups/{code}` - Atualizar grupo
- `DELETE /api/groups/{code}` - Desabilitar grupo
- `POST /api/groups/{groupCode}/members` - Adicionar membros
- `DELETE /api/groups/{groupCode}/members/{userCode}` - Remover membro

### Despesas
- `POST /api/expenses` - Criar despesa (publica mensagens na fila)
- `GET /api/expenses/{code}` - Buscar despesa
- `PUT /api/expenses/{code}` - Atualizar despesa
- `DELETE /api/expenses/{code}` - Desabilitar despesa
- `GET /api/expenses/group/{groupCode}` - Listar despesas do grupo

## 📝 Testando o Fluxo Completo

1. **Criar um usuário**
   ```bash
   POST /api/users
   ```

2. **Criar um grupo**
   ```bash
   POST /api/groups?creatorCode={userCode}
   ```

3. **Adicionar membros ao grupo**
   ```bash
   POST /api/groups/{groupCode}/members
   ```

4. **Criar uma despesa**
   ```bash
   POST /api/expenses
   ```

5. **Monitorar no RabbitMQ Management**
   - Acesse http://localhost:15672
   - Veja as mensagens sendo processadas nas filas
   - Acompanhe os logs da aplicação

## 🎯 Próximas Implementações

- [ ] Estratégias de divisão (proporcional, customizada)
- [ ] Envio real de notificações (email/SMS)
- [ ] Webhook para notificações
- [ ] Dashboard de monitoramento
- [ ] Retry manual de mensagens na DLQ via endpoint
- [ ] Status de processamento na Expense
- [ ] Autenticação JWT
- [ ] Paginação nos endpoints de listagem

## 🛑 Parar os Serviços

```bash
docker-compose down
```

Para remover também os volumes (dados serão perdidos):
```bash
docker-compose down -v
```

## 📚 Referências

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring AMQP Documentation](https://docs.spring.io/spring-amqp/reference/)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Gradle Build Tool](https://docs.gradle.org)

## 📄 Licença

Este projeto é de código aberto para fins educacionais.

