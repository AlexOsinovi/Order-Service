package by.osinovi.orderservice.dto.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage {
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
}