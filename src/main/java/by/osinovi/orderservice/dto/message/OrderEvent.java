package by.osinovi.orderservice.dto.message;

import by.osinovi.orderservice.util.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private Long orderId;
    private Long userId;
    private OrderStatus status;
    private LocalDate creationDate;
    private BigDecimal totalAmount;
    private List<OrderItemData> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemData {
        private Long itemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal price;
    }
}