package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.message.PaymentMessage;
import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderWithUserResponseDto;
import jakarta.transaction.Transactional;

public interface OrderCommandService {
    @Transactional
    OrderWithUserResponseDto createOrder(OrderRequestDto orderRequestDto);

    @Transactional
    OrderWithUserResponseDto updateOrder(Long id, OrderRequestDto orderRequestDto);

    @Transactional
    void deleteOrder(Long id);

    @Transactional
    void processPayment(PaymentMessage paymentMessage);
}
