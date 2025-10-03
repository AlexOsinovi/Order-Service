package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.dto.message.PaymentMessage;
import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderResponseDto;
import by.osinovi.orderservice.dto.order.OrderWithUserResponseDto;
import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import by.osinovi.orderservice.entity.Item;
import by.osinovi.orderservice.entity.Order;
import by.osinovi.orderservice.entity.OrderItem;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.kafka.OrderProducer;
import by.osinovi.orderservice.mapper.OrderItemMapper;
import by.osinovi.orderservice.mapper.OrderMapper;
import by.osinovi.orderservice.repository.ItemRepository;
import by.osinovi.orderservice.repository.OrderRepository;
import by.osinovi.orderservice.service.OrderService;
import by.osinovi.orderservice.service.UserInfoService;
import by.osinovi.orderservice.util.OrderStatus;
import by.osinovi.orderservice.util.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserInfoService userInfoService;
    private final OrderProducer orderProducer;

    @Override
    @Transactional
    public OrderWithUserResponseDto createOrder(OrderRequestDto orderRequestDto) {
        Order order = orderMapper.toEntity(orderRequestDto);
        order.setStatus(OrderStatus.CREATED);

        List<OrderItem> processedItems = order.getOrderItems().stream()
                .peek(orderItem -> {
                    Item itemFromDb = itemRepository.findById(orderItem.getItem().getId())
                            .orElseThrow(() -> new NotFoundException("Item not found with id: " + orderItem.getItem().getId()));

                    orderItem.setItem(itemFromDb);

                    orderItem.setOrder(order);

                })
                .toList();

        order.setOrderItems(processedItems);


        Order saved = orderRepository.save(order);

        BigDecimal totalAmount = calculateTotalAmount(saved);

        orderProducer.sendCreateOrderEvent(orderMapper.toMessage(saved, totalAmount));

        UserInfoResponseDto user = userInfoService.getUserInfoById(saved.getUserId());

        OrderResponseDto orderResponse = orderMapper.toResponse(saved);

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
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order with ID " + id + " not found"));

        existing.setUserId(orderRequestDto.getUserId());
        existing.setCreationDate(orderRequestDto.getCreationDate());

        existing.getOrderItems().clear();

        List<OrderItem> newOrderItems = orderRequestDto.getOrderItems().stream()
                .map(orderItemMapper::toEntity)
                .peek(newOrderItem -> {
                    Item itemFromDb = itemRepository.findById(newOrderItem.getItem().getId())
                            .orElseThrow(() -> new NotFoundException("Item not found with id: " + newOrderItem.getItem().getId()));

                    newOrderItem.setItem(itemFromDb);
                    newOrderItem.setOrder(existing);
                })
                .toList();

        existing.getOrderItems().addAll(newOrderItems);

        existing.setStatus(OrderStatus.CHANGED);

        Order updated = orderRepository.save(existing);

        BigDecimal totalAmount = calculateTotalAmount(updated);
        orderProducer.sendCreateOrderEvent(orderMapper.toMessage(updated, totalAmount));
        OrderResponseDto orderResponse = orderMapper.toResponse(updated);
        UserInfoResponseDto user = userInfoService.getUserInfoById(updated.getUserId());

        return new OrderWithUserResponseDto(orderResponse, user);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException("Order with ID " + id + " not found");
        }
        orderRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void processPayment(PaymentMessage paymentMessage) {
        orderRepository.findById(paymentMessage.getOrderId()).ifPresentOrElse(order -> {
            order.setStatus(paymentMessage.getStatus().equals(PaymentStatus.SUCCESS) ? OrderStatus.PAID : OrderStatus.FAILED);
            order.setPaymentId(paymentMessage.getId());
            orderRepository.save(order);
            log.info("Updated order {} with status {}", paymentMessage.getOrderId(), order.getStatus());
        }, () -> log.error("Cannot find order with id {} for starting processing", paymentMessage.getOrderId()));
    }

    private BigDecimal calculateTotalAmount(Order order) {
        return order.getOrderItems().stream()
                .filter(item -> item.getItem() != null && item.getItem().getPrice() != null)
                .map(item -> item.getItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}