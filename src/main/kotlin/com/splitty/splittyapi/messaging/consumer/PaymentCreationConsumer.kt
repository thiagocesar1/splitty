package com.splitty.splittyapi.messaging.consumer

import com.splitty.splittyapi.messaging.config.RabbitMQConfig
import com.splitty.splittyapi.messaging.dto.CreatePaymentMessage
import com.splitty.splittyapi.messaging.dto.NotificationType
import com.splitty.splittyapi.messaging.dto.PaymentNotificationMessage
import com.splitty.splittyapi.messaging.producer.PaymentMessageProducer
import com.splitty.splittyapi.payments.service.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("consumer")
class PaymentCreationConsumer(
    private val paymentService: PaymentService,
    private val messageProducer: PaymentMessageProducer
) {
    private val logger = LoggerFactory.getLogger(PaymentCreationConsumer::class.java)

    @RabbitListener(queues = [RabbitMQConfig.EXPENSE_PAYMENT_CREATE_QUEUE])
    fun processPaymentCreation(message: CreatePaymentMessage) {
        try {
            logger.info("Processing payment creation for expense: ${message.expenseCode}, member: ${message.memberUserCode}")

            val payment = paymentService.createPaymentForMember(
                expenseCode = message.expenseCode,
                groupId = message.groupId,
                memberId = message.memberId,
                chargedValue = message.chargedValue
            )

            logger.info("Created payment ${payment.code} for member ${message.memberUserCode} with value ${message.chargedValue}")


            val notificationMessage = PaymentNotificationMessage(
                paymentCode = payment.code,
                expenseCode = message.expenseCode,
                memberUserId = message.memberId,
                chargedValue = payment.chargedValue,
                expenseDescription = message.expenseDescription,
                dueDate = message.expenseDueDate,
                notificationType = NotificationType.PAYMENT_CREATED
            )

            messageProducer.sendPaymentNotificationMessage(notificationMessage)

            logger.info("Successfully processed payment creation for expense: ${message.expenseCode}, member: ${message.memberUserCode}")
        } catch (e: Exception) {
            logger.error("Error processing payment creation for expense: ${message.expenseCode}, member: ${message.memberUserCode}", e)
            throw e
        }
    }
}
