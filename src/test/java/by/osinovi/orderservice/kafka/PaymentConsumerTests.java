package by.osinovi.orderservice.kafka;

import by.osinovi.orderservice.dto.message.PaymentMessage;
import by.osinovi.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentConsumerTests {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentConsumer paymentConsumer;

    @Test
    void handleCreatePayment_success() {
        UUID paymentId = UUID.randomUUID();
        PaymentMessage paymentMessage = new PaymentMessage(paymentId, 1L, 100L,
                by.osinovi.orderservice.util.PaymentStatus.SUCCESS, BigDecimal.valueOf(50.0));

        paymentConsumer.handleCreatePayment(paymentMessage);

        verify(orderService).processPayment(paymentMessage);
    }

    @Test
    void handleCreatePayment_failed() {
        UUID paymentId = UUID.randomUUID();
        PaymentMessage paymentMessage = new PaymentMessage(paymentId, 1L, 100L,
                by.osinovi.orderservice.util.PaymentStatus.FAILED, BigDecimal.valueOf(50.0));

        paymentConsumer.handleCreatePayment(paymentMessage);

        verify(orderService).processPayment(paymentMessage);
    }
}