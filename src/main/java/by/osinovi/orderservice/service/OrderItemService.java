package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.orderItem.OrderItemRequestDto;
import by.osinovi.orderservice.dto.orderItem.OrderItemResponseDto;
import java.util.List;

public interface OrderItemService {
    OrderItemResponseDto createOrderItem(OrderItemRequestDto orderItemRequestDto, Long orderId);
    OrderItemResponseDto getOrderItemById(Long id);
    List<OrderItemResponseDto> getOrderItemsByOrderId(Long orderId);
    OrderItemResponseDto updateOrderItem(Long id, OrderItemRequestDto orderItemRequestDto);
    void deleteOrderItem(Long id);
}