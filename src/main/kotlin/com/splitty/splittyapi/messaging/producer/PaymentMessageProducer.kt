package com.splitty.splittyapi.messaging.producer

import com.splitty.splittyapi.messaging.config.RabbitMQConfig
import com.splitty.splittyapi.messaging.dto.CreatePaymentMessage
import com.splitty.splittyapi.messaging.dto.PaymentNotificationMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("api", "consumer")
class PaymentMessageProducer(
    private val rabbitTemplate: RabbitTemplate
) {
    private val logger = LoggerFactory.getLogger(PaymentMessageProducer::class.java)

    fun sendCreatePaymentMessage(message: CreatePaymentMessage) {
        try {
            logger.info("Publishing create payment message for expense: ${message.expenseCode}")
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXPENSE_EXCHANGE,
                RabbitMQConfig.EXPENSE_PAYMENT_ROUTING_KEY,
                message
            )
            logger.info("Successfully published create payment message for expense: ${message.expenseCode}")
        } catch (e: Exception) {
            logger.error("Error publishing create payment message for expense: ${message.expenseCode}", e)
            throw e
        }
    }

    fun sendPaymentNotificationMessage(message: PaymentNotificationMessage) {
        try {
            logger.info("Publishing payment notification for payment: ${message.paymentCode}")
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.PAYMENT_NOTIFICATION_ROUTING_KEY,
                message
            )
            logger.info("Successfully published payment notification for payment: ${message.paymentCode}")
        } catch (e: Exception) {
            logger.error("Error publishing payment notification for payment: ${message.paymentCode}", e)
            throw e
        }
    }
}

