package by.osinovi.orderservice.kafka;

import by.osinovi.orderservice.dto.message.PaymentMessage;
import by.osinovi.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {
    private final OrderService orderService;

    @KafkaListener(topics = "${spring.kafka.topics.payments}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleCreatePayment(PaymentMessage paymentMessage) {
        log.info("Processing payment {} with status {} for order {}", paymentMessage.getId(), paymentMessage.getStatus(), paymentMessage.getOrderId());
        orderService.processPayment(paymentMessage);
    }
}