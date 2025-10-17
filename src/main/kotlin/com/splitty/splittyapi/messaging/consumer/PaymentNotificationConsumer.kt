package com.splitty.splittyapi.messaging.consumer

import com.splitty.splittyapi.messaging.config.RabbitMQConfig
import com.splitty.splittyapi.messaging.dto.PaymentNotificationMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("consumer")
class PaymentNotificationConsumer {
    private val logger = LoggerFactory.getLogger(PaymentNotificationConsumer::class.java)

    @RabbitListener(queues = [RabbitMQConfig.PAYMENT_NOTIFICATION_QUEUE])
    fun processPaymentNotification(message: PaymentNotificationMessage) {
        try {
            logger.info("Processing payment notification: ${message.notificationType} for payment: ${message.paymentCode}")

            when (message.notificationType) {
                com.splitty.splittyapi.messaging.dto.NotificationType.PAYMENT_CREATED -> {
                    logger.info("Sending payment created notification to user ${message.memberUserId}")
                }
                com.splitty.splittyapi.messaging.dto.NotificationType.PAYMENT_REMINDER -> {
                    logger.info("Sending payment reminder to user ${message.memberUserId}")
                }
                com.splitty.splittyapi.messaging.dto.NotificationType.PAYMENT_OVERDUE -> {
                    logger.info("Sending payment overdue notification to user ${message.memberUserId}")
                }
                com.splitty.splittyapi.messaging.dto.NotificationType.PAYMENT_CONFIRMED -> {
                    logger.info("Sending payment confirmation to user ${message.memberUserId}")
                }
            }

            logger.info("Successfully processed notification for payment: ${message.paymentCode}")
        } catch (e: Exception) {
            logger.error("Error processing payment notification for payment: ${message.paymentCode}", e)
            throw e
        }
    }
}

