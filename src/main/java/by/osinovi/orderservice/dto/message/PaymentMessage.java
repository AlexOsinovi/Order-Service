package by.osinovi.orderservice.dto.message;

import by.osinovi.orderservice.util.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMessage {
    private UUID id;
    private Long orderId;
    private Long userId;
    private PaymentStatus status;
    private BigDecimal paymentAmount;
}