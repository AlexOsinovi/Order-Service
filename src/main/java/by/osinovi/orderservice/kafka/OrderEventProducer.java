//package by.osinovi.orderservice.kafka;
//
//import by.osinovi.orderservice.dto.message.OrderEvent;
//import by.osinovi.orderservice.dto.message.OrderMessage;
//import by.osinovi.orderservice.entity.Order;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class OrderEventProducer {
//    private final KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate;
//
//    @Value("${spring.kafka.topics.order-events}")
//    private String orderEventsTopic;
//
//    public void sendCreateOrderEventToMongo(Order order,OrderEvent event) {
//        orderEventKafkaTemplate.send(orderEventsTopic, order.getId().toString(), event);
//        log.info("Published OrderEvent for order ID: {}", order.getId());
//    }
//
//}