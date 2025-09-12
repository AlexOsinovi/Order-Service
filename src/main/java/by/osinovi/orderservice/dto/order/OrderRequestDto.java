package by.osinovi.orderservice.dto.order;

import by.osinovi.orderservice.dto.order_item.OrderItemRequestDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Creation date cannot be null")
    private LocalDate creationDate;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequestDto> orderItems = new ArrayList<>();
}