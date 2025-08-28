package by.osinovi.orderservice.dto.order;

import by.osinovi.orderservice.dto.orderItem.OrderItemResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long id;
    private Long userId;
    private String status;
    private LocalDate creationDate;
    private List<OrderItemResponseDto> orderItems = new ArrayList<>();
}