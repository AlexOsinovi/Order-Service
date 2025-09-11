package by.osinovi.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicsConfig {
    private final String ordersTopic;
    private final String paymentsTopic;
    private final String deadPaymentsTopic;

    public TopicsConfig(@Value("${spring.kafka.topics.orders}") String ordersTopic,
                       @Value("${spring.kafka.topics.payments}") String paymentsTopic,
                       @Value("${spring.kafka.topics.dead-payments}") String deadPaymentsTopic) {
        this.ordersTopic = ordersTopic;
        this.paymentsTopic = paymentsTopic;
        this.deadPaymentsTopic = deadPaymentsTopic;
    }

    @Bean
    public NewTopic createOrdersTopic() {
        return TopicBuilder.name(ordersTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic createPaymentsTopic() {
        return TopicBuilder.name(paymentsTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic createDeadPaymentsTopic() {
        return TopicBuilder.name(deadPaymentsTopic).partitions(1).replicas(1).build();
    }
}