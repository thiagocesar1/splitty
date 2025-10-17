package com.splitty.splittyapi.messaging.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig {

    companion object {
        const val EXPENSE_PAYMENT_CREATE_QUEUE = "expense.payment.create"
        const val EXPENSE_PAYMENT_CREATE_DLQ = "expense.payment.create.dlq"
        const val PAYMENT_NOTIFICATION_QUEUE = "payment.notification"
        const val PAYMENT_NOTIFICATION_DLQ = "payment.notification.dlq"

        const val EXPENSE_EXCHANGE = "expense.exchange"
        const val NOTIFICATION_EXCHANGE = "notification.exchange"
        const val DLX_EXCHANGE = "dlx.exchange"

        const val EXPENSE_PAYMENT_ROUTING_KEY = "expense.payment.create"
        const val PAYMENT_NOTIFICATION_ROUTING_KEY = "payment.notification"
    }


    @Bean
    fun messageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter()
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory)
        rabbitTemplate.messageConverter = messageConverter()
        return rabbitTemplate
    }


    @Bean
    fun expenseExchange(): TopicExchange {
        return TopicExchange(EXPENSE_EXCHANGE)
    }

    @Bean
    fun notificationExchange(): TopicExchange {
        return TopicExchange(NOTIFICATION_EXCHANGE)
    }

    @Bean
    fun dlxExchange(): DirectExchange {
        return DirectExchange(DLX_EXCHANGE)
    }


    @Bean
    fun expensePaymentCreateQueue(): Queue {
        return QueueBuilder.durable(EXPENSE_PAYMENT_CREATE_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", EXPENSE_PAYMENT_CREATE_DLQ)
            .build()
    }

    @Bean
    fun paymentNotificationQueue(): Queue {
        return QueueBuilder.durable(PAYMENT_NOTIFICATION_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", PAYMENT_NOTIFICATION_DLQ)
            .build()
    }


    @Bean
    fun expensePaymentCreateDlq(): Queue {
        return QueueBuilder.durable(EXPENSE_PAYMENT_CREATE_DLQ).build()
    }

    @Bean
    fun paymentNotificationDlq(): Queue {
        return QueueBuilder.durable(PAYMENT_NOTIFICATION_DLQ).build()
    }


    @Bean
    fun expensePaymentCreateBinding(
        expensePaymentCreateQueue: Queue,
        expenseExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(expensePaymentCreateQueue)
            .to(expenseExchange)
            .with(EXPENSE_PAYMENT_ROUTING_KEY)
    }

    @Bean
    fun paymentNotificationBinding(
        paymentNotificationQueue: Queue,
        notificationExchange: TopicExchange
    ): Binding {
        return BindingBuilder
            .bind(paymentNotificationQueue)
            .to(notificationExchange)
            .with(PAYMENT_NOTIFICATION_ROUTING_KEY)
    }

    @Bean
    fun expensePaymentCreateDlqBinding(
        expensePaymentCreateDlq: Queue,
        dlxExchange: DirectExchange
    ): Binding {
        return BindingBuilder
            .bind(expensePaymentCreateDlq)
            .to(dlxExchange)
            .with(EXPENSE_PAYMENT_CREATE_DLQ)
    }

    @Bean
    fun paymentNotificationDlqBinding(
        paymentNotificationDlq: Queue,
        dlxExchange: DirectExchange
    ): Binding {
        return BindingBuilder
            .bind(paymentNotificationDlq)
            .to(dlxExchange)
            .with(PAYMENT_NOTIFICATION_DLQ)
    }
}

