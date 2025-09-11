package by.osinovi.orderservice.config;

import by.osinovi.orderservice.dto.message.OrderMessage;
import by.osinovi.orderservice.dto.message.PaymentMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {
    private final String kafkaUri;
    private final String deadPaymentsTopic;

    public KafkaConfig(@Value("${spring.kafka.bootstrap-servers}") String kafkaUri,
                      @Value("${spring.kafka.topics.dead-payments}") String deadPaymentsTopic) {
        this.kafkaUri = kafkaUri;
        this.deadPaymentsTopic = deadPaymentsTopic;
    }

    @Bean
    public ConsumerFactory<String, PaymentMessage> paymentConsumerFactory() {
        JsonDeserializer<PaymentMessage> deserializer = new JsonDeserializer<>(PaymentMessage.class, false);
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUri);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentMessage> paymentListenerContainerFactory(
            KafkaTemplate<String, PaymentMessage> dlqKafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentMessage> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentConsumerFactory());
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dlqKafkaTemplate, (message, exception) -> new TopicPartition(deadPaymentsTopic, message.partition()));
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3)));
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    public ProducerFactory<String, OrderMessage> orderProducerFactory() {
        return new DefaultKafkaProducerFactory<>(defaultProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, OrderMessage> orderKafkaTemplate() {
        return new KafkaTemplate<>(orderProducerFactory());
    }

    @Bean
    public ProducerFactory<String, PaymentMessage> dlqProducerFactory() {
        return new DefaultKafkaProducerFactory<>(defaultProducerConfig());
    }

    @Bean
    public KafkaTemplate<String, PaymentMessage> dlqKafkaTemplate() {
        return new KafkaTemplate<>(dlqProducerFactory());
    }

    private Map<String, Object> defaultProducerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUri);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }
}