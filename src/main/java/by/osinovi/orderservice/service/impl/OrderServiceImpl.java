package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.dto.message.PaymentMessage;
import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderResponseDto;
import by.osinovi.orderservice.dto.order.OrderWithUserResponseDto;
import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import by.osinovi.orderservice.entity.Order;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.kafka.OrderProducer;
import by.osinovi.orderservice.mapper.OrderItemMapper;
import by.osinovi.orderservice.mapper.OrderMapper;
import by.osinovi.orderservice.repository.OrderRepository;
import by.osinovi.orderservice.service.OrderService;
import by.osinovi.orderservice.service.UserInfoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserInfoService userInfoService;
    private final OrderProducer orderProducer;

    @Override
    @Transactional
    public OrderWithUserResponseDto createOrder(OrderRequestDto orderRequestDto) {
        Order order = orderMapper.toEntity(orderRequestDto);
        order.getOrderItems().forEach(item -> item.setOrder(order));
        Order saved = orderRepository.save(order);
        OrderResponseDto orderResponse = orderMapper.toResponse(saved);

        orderProducer.sendCreateOrderEvent(orderMapper.toMessage(saved));

        UserInfoResponseDto user = userInfoService.getUserInfoById(saved.getUserId());
        return new OrderWithUserResponseDto(orderResponse, user);
    }

    @Override
    public OrderWithUserResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order with " + id + " not found"));
        OrderResponseDto orderResponse = orderMapper.toResponse(order);
        UserInfoResponseDto user = userInfoService.getUserInfoById(order.getUserId());
        return new OrderWithUserResponseDto(orderResponse, user);
    }

    @Override
    public List<OrderWithUserResponseDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> {
                    try {
                        OrderResponseDto orderResponse = orderMapper.toResponse(order);
                        UserInfoResponseDto user = userInfoService.getUserInfoById(order.getUserId());
                        return new OrderWithUserResponseDto(orderResponse, user);
                    } catch (NotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<OrderWithUserResponseDto> getOrdersByStatuses(List<String> statuses) {
        return orderRepository.findByStatuses(statuses).stream()
                .map(order -> {
                    try {
                        OrderResponseDto orderResponse = orderMapper.toResponse(order);
                        UserInfoResponseDto user = userInfoService.getUserInfoById(order.getUserId());
                        return new OrderWithUserResponseDto(orderResponse, user);
                    } catch (NotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
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

    @Override
    @Transactional
    public void addPaymentId(PaymentMessage paymentMessage) {
        orderRepository.findById(paymentMessage.getOrderId()).ifPresentOrElse(order -> {
            if (order.getPaymentId() == null) {
                order.setPaymentId(paymentMessage.getPaymentId());
                order.setStatus("TO_PAY");
                orderRepository.save(order);
                log.info("Attached payment with id {} to order with id {}", paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            } else {
                log.warn("Payment with id {} already attached to order with id {}", paymentMessage.getPaymentId(), paymentMessage.getOrderId());
            }
        }, () -> log.error("Cannot find order with id {} for attaching payment", paymentMessage.getOrderId()));
    }

    @Override
    @Transactional
    public void processSuccessPayment(PaymentMessage paymentMessage) {
        orderRepository.findById(paymentMessage.getOrderId()).ifPresentOrElse(order -> {
            order.setStatus("IN_PROGRESS");
            orderRepository.save(order);
            log.info("Order with id {} was successfully paid", paymentMessage.getOrderId());
        }, () -> log.error("Cannot find order with id {} for starting processing", paymentMessage.getOrderId()));
    }
}