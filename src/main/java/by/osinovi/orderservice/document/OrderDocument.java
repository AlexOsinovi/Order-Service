package by.osinovi.orderservice.document;

import by.osinovi.orderservice.util.OrderStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Document("orders_read_model")
public class OrderDocument {
    @Id
    private Long id;
    private OrderStatus status;
    private LocalDate creationDate;
    private UUID paymentId;
    private BigDecimal totalAmount;

    private UserInfo user;
    private List<OrderItemInfo> items;

    @Data
    public static class UserInfo {
        private Long id;
        private String name;
        private String surname;
        private String email;
    }

    @Data
    public static class OrderItemInfo {
        private Long itemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal itemPrice;
    }
}