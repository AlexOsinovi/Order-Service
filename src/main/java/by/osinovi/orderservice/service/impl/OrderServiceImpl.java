package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderResponseDto;
import by.osinovi.orderservice.dto.order.OrderWithUserResponseDto;
import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import by.osinovi.orderservice.entity.Order;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.mapper.OrderItemMapper;
import by.osinovi.orderservice.mapper.OrderMapper;
import by.osinovi.orderservice.repository.OrderRepository;
import by.osinovi.orderservice.service.OrderService;
import by.osinovi.orderservice.service.UserInfoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserInfoService userInfoService;

    @Override
    public OrderWithUserResponseDto createOrder(OrderRequestDto orderRequestDto) {
        Order order = orderMapper.toEntity(orderRequestDto);
        order.getOrderItems().forEach(item -> item.setOrder(order));
        Order saved = orderRepository.save(order);
        OrderResponseDto orderResponse = orderMapper.toResponse(saved);
        UserInfoResponseDto user = userInfoService.getUserInfoById(saved.getUserId());
        return new OrderWithUserResponseDto(orderResponse, user);
    }

    @Override
    public OrderWithUserResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order with ID " + id + " not found"));
        OrderResponseDto orderResponse = orderMapper.toResponse(order);
        UserInfoResponseDto user = userInfoService.getUserInfoById(order.getUserId());
        return new OrderWithUserResponseDto(orderResponse, user);
    }

    @Override
    public List<OrderWithUserResponseDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> {
                    OrderResponseDto orderResponse = orderMapper.toResponse(order);
                    UserInfoResponseDto user = userInfoService.getUserInfoById(order.getUserId());
                    return new OrderWithUserResponseDto(orderResponse, user);
                })
                .toList();
    }

    @Override
    public List<OrderWithUserResponseDto> getOrdersByStatuses(List<String> statuses) {
        return orderRepository.findByStatuses(statuses).stream()
                .map(order -> {
                    OrderResponseDto orderResponse = orderMapper.toResponse(order);
                    UserInfoResponseDto user = userInfoService.getUserInfoById(order.getUserId());
                    return new OrderWithUserResponseDto(orderResponse, user);
                })
                .toList();
    }

    @Override
    @Transactional
    public OrderWithUserResponseDto updateOrder(Long id, OrderRequestDto orderRequestDto) {
        Order existing = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order with ID " + id + " not found"));
        existing.setUserId(orderRequestDto.getUserId());
        existing.setStatus(orderRequestDto.getStatus());
        existing.setCreationDate(orderRequestDto.getCreationDate());
        existing.getOrderItems().clear();
        orderRequestDto.getOrderItems().stream()
                .map(orderItemMapper::toEntity)
                .forEach(item -> {
                    item.setOrder(existing);
                    existing.getOrderItems().add(item);
                });
        Order updated = orderRepository.save(existing);
        OrderResponseDto orderResponse = orderMapper.toResponse(updated);
        UserInfoResponseDto user = userInfoService.getUserInfoById(updated.getUserId());
        return new OrderWithUserResponseDto(orderResponse, user);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException("Order with ID " + id + " not found");
        }
        orderRepository.deleteById(id);
    }
}