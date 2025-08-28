package by.osinovi.orderservice.dto.order;

import by.osinovi.orderservice.dto.order_item.OrderItemRequestDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Status cannot be blank")
    @Size(max = 32, message = "Status must be less than 32 characters")
    private String status;

    @NotNull(message = "Creation date cannot be null")
    private LocalDate creationDate;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequestDto> orderItems = new ArrayList<>();
}