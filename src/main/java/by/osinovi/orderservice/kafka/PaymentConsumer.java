package by.osinovi.orderservice.kafka;

import by.osinovi.orderservice.dto.message.PaymentMessage;
import by.osinovi.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {
    private final OrderService orderService;

    @KafkaListener(topics = "${spring.kafka.topics.payments}", groupId = "order-group",
            containerFactory = "paymentListenerContainerFactory")
    public void handleCreatePayment(PaymentMessage paymentMessage, Acknowledgment ack) {
        try {
            switch (paymentMessage.getStatus()) {
                case "CREATED":
                    orderService.addPaymentId(paymentMessage);
                    break;
                case "SUCCESS":
                    orderService.processSuccessPayment(paymentMessage);
                    break;
                default:
                    log.warn("No processing for payment status: {}", paymentMessage.getStatus());
            }
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Error handling CREATE_PAYMENT for orderId: {}", paymentMessage.getOrderId(), e);
            throw new RuntimeException("Failed to handle CREATE_PAYMENT", e);
        }
    }
}