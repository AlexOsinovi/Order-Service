package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderResponseDto;
import by.osinovi.orderservice.dto.order.OrderWithUserResponseDto;
import by.osinovi.orderservice.dto.userInfo.UserInfoResponseDto;
import by.osinovi.orderservice.entity.Order;
import by.osinovi.orderservice.mapper.OrderItemMapper;
import by.osinovi.orderservice.mapper.OrderMapper;
import by.osinovi.orderservice.repository.OrderRepository;
import by.osinovi.orderservice.service.OrderService;
import by.osinovi.orderservice.service.UserInfoService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private UserInfoService userInfoService;

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
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
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
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderWithUserResponseDto> getOrdersByStatuses(List<String> statuses) {
        return orderRepository.findByStatuses(statuses).stream()
                .map(order -> {
                    OrderResponseDto orderResponse = orderMapper.toResponse(order);
                    UserInfoResponseDto user = userInfoService.getUserInfoById(order.getUserId());
                    return new OrderWithUserResponseDto(orderResponse, user);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderWithUserResponseDto updateOrder(Long id, OrderRequestDto orderRequestDto) {
        Order existing = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
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
        orderRepository.deleteById(id);
    }
}