package by.osinovi.orderservice.dto.orderItem;

import by.osinovi.orderservice.dto.item.ItemResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDto {
    private Long id;
    private ItemResponseDto item;
    private Integer quantity;
}