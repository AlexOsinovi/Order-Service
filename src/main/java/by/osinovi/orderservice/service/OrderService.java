package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderWithUserResponseDto;

import java.util.List;

public interface OrderService {
    OrderWithUserResponseDto createOrder(OrderRequestDto orderRequestDto);
    OrderWithUserResponseDto getOrderById(Long id);
    List<OrderWithUserResponseDto> getAllOrders();
    List<OrderWithUserResponseDto> getOrdersByStatuses(List<String> statuses);
    OrderWithUserResponseDto updateOrder(Long id, OrderRequestDto orderRequestDto);
    void deleteOrder(Long id);
}