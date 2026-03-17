package by.osinovi.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderDeletedEventProducer {
    private final KafkaTemplate<String, Long> orderDeletedKafkaTemplate;


    @Value("${spring.kafka.topics.order-deleted}")
    private String orderDeletedTopic;

    public void sendCreateOrderDeletedEvent(Long id) {
        orderDeletedKafkaTemplate.send(orderDeletedTopic, id.toString(), id);
        log.info("Published OrderDeletedEvent for order ID: {}", id);
    }

}