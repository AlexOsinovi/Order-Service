package by.osinovi.orderservice.kafka;

import by.osinovi.orderservice.dto.message.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProducer {
    private final KafkaTemplate<String, OrderMessage> orderKafkaTemplate;

    @Value("${spring.kafka.topics.orders}")
    private String ordersTopic;

    public void sendCreateOrderEvent(OrderMessage orderMessage) {
        orderKafkaTemplate.send(ordersTopic, orderMessage.getOrderId().toString(), orderMessage)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send CREATE_ORDER event for orderId: {}", orderMessage.getOrderId(), ex);
                        throw new RuntimeException("Failed to send CREATE_ORDER event", ex);
                    }
                    log.info("CREATE_ORDER event sent for orderId: {}", orderMessage.getOrderId());
                });
        /// TODO: handle failure scenarios appropriately
    }
}