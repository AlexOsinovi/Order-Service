package by.osinovi.orderservice.dto.order_item;

import by.osinovi.orderservice.dto.item.ItemResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDto {
    private Long id;
    private ItemResponseDto item;
    private Integer quantity;
}