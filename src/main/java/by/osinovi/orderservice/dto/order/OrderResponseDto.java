package by.osinovi.orderservice.dto.order;

import by.osinovi.orderservice.dto.order_item.OrderItemResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private UUID paymentId;
    private String status;
    private LocalDate creationDate;
    private List<OrderItemResponseDto> orderItems = new ArrayList<>();
}