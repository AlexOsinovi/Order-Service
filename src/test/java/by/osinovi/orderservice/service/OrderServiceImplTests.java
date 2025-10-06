package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.message.OrderMessage;
import by.osinovi.orderservice.dto.message.PaymentMessage;
import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderResponseDto;
import by.osinovi.orderservice.dto.order.OrderWithUserResponseDto;
import by.osinovi.orderservice.dto.order_item.OrderItemRequestDto;
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
import by.osinovi.orderservice.service.impl.OrderServiceImpl;
import by.osinovi.orderservice.util.OrderStatus;
import by.osinovi.orderservice.util.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceImplTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private UserInfoService userInfoService;

    @Mock
    private OrderProducer orderProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_success() {
        OrderItemRequestDto itemReq = new OrderItemRequestDto(1L, 2);
        OrderRequestDto req = new OrderRequestDto(100L, LocalDate.now(), List.of(itemReq));
        Order entity = new Order();
        entity.setUserId(100L);
        Order saved = new Order();
        saved.setId(5L);
        saved.setUserId(100L);
        saved.setStatus(OrderStatus.CREATED);
        Item item = new Item();
        item.setPrice(BigDecimal.valueOf(50.0));
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(2);
        saved.setOrderItems(List.of(orderItem));
        OrderResponseDto orderResp = new OrderResponseDto(5L, 100L, null, OrderStatus.CREATED, req.getCreationDate(), List.of());
        UserInfoResponseDto userResp = new UserInfoResponseDto(100L, "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com");
        BigDecimal expectedTotalAmount = BigDecimal.valueOf(100.0); // 50 * 2
        OrderMessage orderMessage = new OrderMessage(5L, 100L, expectedTotalAmount);

        when(orderMapper.toEntity(req)).thenReturn(entity);
        when(orderRepository.save(entity)).thenReturn(saved);
        when(orderMapper.toResponse(saved)).thenReturn(orderResp);
        when(orderMapper.toMessage(saved, expectedTotalAmount)).thenReturn(orderMessage);
        when(userInfoService.getUserInfoById(100L)).thenReturn(userResp);

        OrderWithUserResponseDto result = orderService.createOrder(req);

        assertThat(result.getOrder().getId()).isEqualTo(5L);
        assertThat(result.getUser().getId()).isEqualTo(100L);
        assertThat(result.getOrder().getStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orderProducer).sendCreateOrderEvent(orderMessage);
        verify(orderMapper).toMessage(saved, expectedTotalAmount);
    }

    @Test
    void getOrderById_found() {
        Order order = new Order();
        order.setId(7L);
        order.setUserId(200L);
        order.setStatus(OrderStatus.CREATED);
        OrderResponseDto orderResp = new OrderResponseDto(7L, 200L, null, OrderStatus.CREATED, LocalDate.now(), List.of());
        UserInfoResponseDto userResp = new UserInfoResponseDto(200L, "A", "B", LocalDate.of(1990, 1, 1), "a@b.c");

        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResp);
        when(userInfoService.getUserInfoById(200L)).thenReturn(userResp);

        OrderWithUserResponseDto result = orderService.getOrderById(7L);
        assertThat(result.getOrder().getId()).isEqualTo(7L);
        assertThat(result.getUser().getId()).isEqualTo(200L);
        assertThat(result.getOrder().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void getOrderById_notFound() {
        when(orderRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getOrderById(9L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order with 9 not found");
    }

    @Test
    void getAllOrders_mapsUsers() {
        Order o1 = new Order();
        o1.setId(1L);
        o1.setUserId(11L);
        o1.setStatus(OrderStatus.CREATED);
        Order o2 = new Order();
        o2.setId(2L);
        o2.setUserId(22L);
        o2.setStatus(OrderStatus.PAID);
        when(orderRepository.findAll()).thenReturn(List.of(o1, o2));
        when(orderMapper.toResponse(o1)).thenReturn(new OrderResponseDto(1L, 11L, null, OrderStatus.CREATED, LocalDate.now(), List.of()));
        when(orderMapper.toResponse(o2)).thenReturn(new OrderResponseDto(2L, 22L, null, OrderStatus.PAID, LocalDate.now(), List.of()));
        when(userInfoService.getUserInfoById(11L)).thenReturn(new UserInfoResponseDto(11L, "n", "s", LocalDate.now(), "e1"));
        when(userInfoService.getUserInfoById(22L)).thenReturn(new UserInfoResponseDto(22L, "n", "s", LocalDate.now(), "e2"));

        var result = orderService.getAllOrders();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrder().getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(result.get(1).getOrder().getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void getOrdersByStatuses_filters() {
        Order o = new Order();
        o.setId(1L);
        o.setUserId(11L);
        o.setStatus(OrderStatus.CREATED);
        when(orderRepository.findByStatuses(List.of("CREATED", "PAID"))).thenReturn(List.of(o));
        when(orderMapper.toResponse(o)).thenReturn(new OrderResponseDto(1L, 11L, null, OrderStatus.CREATED, LocalDate.now(), List.of()));
        when(userInfoService.getUserInfoById(11L)).thenReturn(new UserInfoResponseDto(11L, "n", "s", LocalDate.now(), "e"));

        var result = orderService.getOrdersByStatuses(List.of("CREATED", "PAID"));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrder().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void updateOrder_success() {
        // --- ARRANGE ---
        long orderId = 55L;
        long newUserId = 300L;
        long itemId = 9L;
        int quantity = 3;

        // Input DTOs
        OrderItemRequestDto itemRequest = new OrderItemRequestDto(itemId, quantity);
        OrderRequestDto requestDto = new OrderRequestDto(newUserId, LocalDate.now(), List.of(itemRequest));

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setOrderItems(new ArrayList<>());

        Item itemFromDb = new Item();
        itemFromDb.setId(itemId);
        itemFromDb.setPrice(new BigDecimal("30.00"));

        OrderItem mappedOrderItem = new OrderItem();
        mappedOrderItem.setItem(new Item(itemId, null, null));
        mappedOrderItem.setQuantity(quantity);

        Order savedOrder = new Order();
        savedOrder.setId(orderId);
        savedOrder.setUserId(newUserId);
        savedOrder.setStatus(OrderStatus.CHANGED);
        OrderItem finalOrderItem = new OrderItem(null, savedOrder, itemFromDb, quantity);
        savedOrder.setOrderItems(List.of(finalOrderItem));

        OrderResponseDto orderResponse = new OrderResponseDto(orderId, newUserId, null, OrderStatus.CHANGED, requestDto.getCreationDate(), List.of());
        UserInfoResponseDto userResponse = new UserInfoResponseDto(newUserId, "N", "S", LocalDate.now(), "e");
        BigDecimal totalAmount = new BigDecimal("90.00");
        OrderMessage orderMessage = new OrderMessage(orderId, newUserId, totalAmount);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemFromDb));
        when(orderItemMapper.toEntity(itemRequest)).thenReturn(mappedOrderItem);
        when(orderRepository.save(existingOrder)).thenReturn(savedOrder);
        when(orderMapper.toResponse(savedOrder)).thenReturn(orderResponse);
        when(userInfoService.getUserInfoById(newUserId)).thenReturn(userResponse);
        when(orderMapper.toMessage(savedOrder, totalAmount)).thenReturn(orderMessage);

        OrderWithUserResponseDto result = orderService.updateOrder(orderId, requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getOrder().getStatus()).isEqualTo(OrderStatus.CHANGED);
        assertThat(result.getOrder().getUserId()).isEqualTo(newUserId);
        assertThat(result.getUser().getId()).isEqualTo(newUserId);

        verify(orderRepository).findById(orderId);
        verify(itemRepository).findById(itemId);
        verify(orderRepository).save(existingOrder);
        verify(orderProducer).sendCreateOrderEvent(orderMessage);
        verify(userInfoService).getUserInfoById(newUserId);
    }

    @Test
    void updateOrder_notFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.updateOrder(1L, new OrderRequestDto(1L, LocalDate.now(), List.of())))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order with ID 1 not found");
    }

    @Test
    void deleteOrder_success() {
        when(orderRepository.existsById(44L)).thenReturn(true);
        orderService.deleteOrder(44L);
        verify(orderRepository).deleteById(44L);
    }

    @Test
    void deleteOrder_notFound() {
        when(orderRepository.existsById(44L)).thenReturn(false);
        assertThatThrownBy(() -> orderService.deleteOrder(44L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order with ID 44 not found");
    }

    @Test
    void processPayment_success() {
        UUID paymentId = UUID.randomUUID();
        PaymentMessage paymentMessage = new PaymentMessage(paymentId, 1L, 100L, PaymentStatus.SUCCESS, BigDecimal.valueOf(100.0));
        Order order = new Order();
        order.setId(1L);
        order.setUserId(100L);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.processPayment(paymentMessage);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaymentId()).isEqualTo(paymentId);
        verify(orderRepository).save(order);
    }

    @Test
    void processPayment_failed() {
        UUID paymentId = UUID.randomUUID();
        PaymentMessage paymentMessage = new PaymentMessage(paymentId, 1L, 100L, PaymentStatus.FAILED, BigDecimal.valueOf(100.0));
        Order order = new Order();
        order.setId(1L);
        order.setUserId(100L);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.processPayment(paymentMessage);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(order.getPaymentId()).isEqualTo(paymentId);
        verify(orderRepository).save(order);
    }

    @Test
    void processPayment_orderNotFound() {
        UUID paymentId = UUID.randomUUID();
        PaymentMessage paymentMessage = new PaymentMessage(paymentId, 999L, 100L, PaymentStatus.SUCCESS, BigDecimal.valueOf(100.0));

        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        orderService.processPayment(paymentMessage);

        verify(orderRepository, never()).save(any());
    }
} 