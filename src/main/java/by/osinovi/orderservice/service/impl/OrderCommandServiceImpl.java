package by.osinovi.orderservice.service.impl;

// --- НУЖНЫЕ ИМПОРТЫ ---
import by.osinovi.orderservice.dto.message.OrderEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.util.stream.Collectors;
// --- ОСТАЛЬНЫЕ ИМПОРТЫ ---
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
import by.osinovi.orderservice.service.OrderCommandService; // <-- ИЗМЕНЕНО: новый интерфейс
import by.osinovi.orderservice.service.UserInfoService;
import by.osinovi.orderservice.util.OrderStatus;
import by.osinovi.orderservice.util.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserInfoService userInfoService;
    private final OrderProducer orderProducer;

    private final KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate;
    private final KafkaTemplate<String, Long> orderDeletedKafkaTemplate;

    @Value("${spring.kafka.topics.order-events}")
    private String orderEventsTopic;

    @Value("${spring.kafka.topics.order-deleted}")
    private String orderDeletedTopic;

    @Transactional
    @Override
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

        // 1. Отправка сообщения для Payment Service (без изменений)
        orderProducer.sendCreateOrderEvent(orderMapper.toMessage(saved, totalAmount));

        // 2. >>> НОВОЕ: Публикация "богатого" события для Проектора (Read Model) <<<
        publishOrderEvent(saved, totalAmount);

        // --- Логика ответа остается (для хорошего UX) ---
        UserInfoResponseDto user = userInfoService.getUserInfoById(saved.getUserId());
        OrderResponseDto orderResponse = orderMapper.toResponse(saved);
        return new OrderWithUserResponseDto(orderResponse, user);
    }

    @Transactional
    @Override
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

        // 2. >>> НОВОЕ: Публикация "богатого" события для Проектора (Read Model) <<<
        publishOrderEvent(updated, totalAmount);

        // --- Логика ответа остается ---
        OrderResponseDto orderResponse = orderMapper.toResponse(updated);
        UserInfoResponseDto user = userInfoService.getUserInfoById(updated.getUserId());
        return new OrderWithUserResponseDto(orderResponse, user);
    }

    @Transactional
    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new NotFoundException("Order with ID " + id + " not found");
        }
        orderRepository.deleteById(id);

        orderDeletedKafkaTemplate.send(orderDeletedTopic, id.toString(), id);

        log.info("Published OrderDeletedEvent for order ID: {}", id);
    }

    @Transactional
    @Override
    public void processPayment(PaymentMessage paymentMessage) {
        // --- ЭТОТ МЕТОД НЕ МЕНЯЕТСЯ ---
        // Он отвечает ТОЛЬКО за обновление Write-базы (Postgres).
        // Read-базу (Mongo) обновит `OrderProjector`, который слушает
        // тот же топик (`payments-reply`)
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

    // --- НОВЫЙ ПРИВАТНЫЙ МЕТОД ---
    /**
     * Собирает полное событие OrderEvent и отправляет его в топик
     * для обновления Read-модели (MongoDB).
     */
    private void publishOrderEvent(Order order, BigDecimal totalAmount) {
        List<OrderEvent.OrderItemData> itemsData = order.getOrderItems().stream()
                .map(oi -> OrderEvent.OrderItemData.builder()
                        .itemId(oi.getItem().getId())
                        .itemName(oi.getItem().getName())
                        .price(oi.getItem().getPrice())
                        .quantity(oi.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderEvent event = OrderEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .creationDate(order.getCreationDate())
                .totalAmount(totalAmount)
                .items(itemsData)
                .build();

        orderEventKafkaTemplate.send(orderEventsTopic, order.getId().toString(), event);
        log.info("Published OrderEvent for order ID: {}", order.getId());
    }
}