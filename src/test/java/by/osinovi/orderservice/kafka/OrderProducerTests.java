package by.osinovi.orderservice.kafka;

import by.osinovi.orderservice.dto.message.OrderMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderProducerTests {

    @Mock
    private KafkaTemplate<String, OrderMessage> orderKafkaTemplate;

    @InjectMocks
    private OrderProducer orderProducer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderProducer, "ordersTopic", "test-orders-topic");
    }

    @Test
    void sendCreateOrderEvent_success() {
        OrderMessage orderMessage = new OrderMessage(1L, 100L, BigDecimal.valueOf(50.0));

        orderProducer.sendCreateOrderEvent(orderMessage);

        verify(orderKafkaTemplate).send(eq("test-orders-topic"), eq("1"), eq(orderMessage));
    }

    @Test
    void sendCreateOrderEvent_withNullOrderId() {
        OrderMessage orderMessage = new OrderMessage(null, 100L, BigDecimal.valueOf(50.0));

        try {
            orderProducer.sendCreateOrderEvent(orderMessage);
        } catch (NullPointerException e) {
            assertThat(e.getMessage()).contains("Cannot invoke \"java.lang.Long.toString()\"");
        }
    }
}
