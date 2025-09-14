package by.osinovi.orderservice.kafka;

import by.osinovi.orderservice.dto.message.OrderMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProducer {
    private final KafkaTemplate<String, OrderMessage> orderKafkaTemplate;

    @Value("${spring.kafka.topics.orders}")
    private String ordersTopic;

    public void sendCreateOrderEvent(OrderMessage orderMessage) {
        orderKafkaTemplate.send(ordersTopic, orderMessage.getOrderId().toString(), orderMessage);
    }

}