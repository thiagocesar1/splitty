# Splitty API - RabbitMQ Integration

## 🚀 Como Iniciar

### 1. Subir os serviços (PostgreSQL + RabbitMQ)

```bash
docker-compose up -d
```

Isso irá iniciar:
- **PostgreSQL** na porta `5432`
- **RabbitMQ** na porta `5672` (AMQP)
- **RabbitMQ Management UI** na porta `15672`

### 2. Acessar RabbitMQ Management

Abra o navegador em: http://localhost:15672

- **Usuário:** splitty
- **Senha:** splitty123

### 3. Executar a aplicação

#### Modo Completo (API + Consumers)
```bash
./gradlew bootRun --args='--spring.profiles.active=api,consumer'
```

#### Apenas API (sem processar filas)
```bash
./gradlew bootRun --args='--spring.profiles.active=api'
```

#### Apenas Consumers (sem API REST)
```bash
./gradlew bootRun --args='--spring.profiles.active=consumer'
```

## 📊 Arquitetura de Mensageria

### Fluxo de Processamento

```
1. POST /api/expenses
   ↓ (síncrono)
2. Expense criada no banco
   ↓ (assíncrono)
3. Mensagem publicada → expense.payment.create
   ↓
4. PaymentCreationConsumer processa
   ↓
5. Payments criados no banco
   ↓
6. Mensagens publicadas → payment.notification
   ↓
7. PaymentNotificationConsumer processa
   ↓
8. Notificações enviadas (logs por enquanto)
```

### Filas Criadas

#### Filas Principais
- **expense.payment.create**: Criação de pagamentos
- **payment.notification**: Notificações de pagamentos

#### Dead Letter Queues (DLQ)
- **expense.payment.create.dlq**: Mensagens que falharam após 3 tentativas
- **payment.notification.dlq**: Notificações que falharam

### Exchanges
- **expense.exchange**: Topic exchange para eventos de despesas
- **notification.exchange**: Topic exchange para notificações
- **dlx.exchange**: Direct exchange para dead letters

## 🔧 Configuração de Retry

- **Tentativas:** 3
- **Intervalo inicial:** 3 segundos
- **Multiplicador:** 2 (3s → 6s → 12s)

## 📝 Testando o Fluxo

1. Crie um grupo e adicione membros
2. Crie uma expense para o grupo
3. Veja nos logs a publicação da mensagem
4. Se o consumer estiver ativo, verá o processamento
5. Acesse RabbitMQ Management para monitorar as filas

## 🎯 Próximas Implementações

- [ ] Estratégias de divisão (proporcional, customizada)
- [ ] Envio real de notificações (email/SMS)
- [ ] Webhook para notificações
- [ ] Dashboard de monitoramento
- [ ] Retry manual de mensagens na DLQ
- [ ] Status de processamento na Expense

## 🐳 Parar os serviços

```bash
docker-compose down
```

Para remover também os volumes:
```bash
docker-compose down -v
```

