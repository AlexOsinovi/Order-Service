package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderResponseDto;
import by.osinovi.orderservice.dto.order.OrderWithUserResponseDto;
import by.osinovi.orderservice.dto.order_item.OrderItemRequestDto;
import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import by.osinovi.orderservice.entity.Order;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.mapper.OrderItemMapper;
import by.osinovi.orderservice.mapper.OrderMapper;
import by.osinovi.orderservice.repository.OrderRepository;
import by.osinovi.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderServiceImplTests {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private UserInfoService userInfoService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createOrder_success() {
        OrderItemRequestDto itemReq = new OrderItemRequestDto(1L, 2);
        OrderRequestDto req = new OrderRequestDto(100L, "NEW", LocalDate.now(), List.of(itemReq));
        Order entity = new Order();
        entity.setUserId(100L);
        Order saved = new Order();
        saved.setId(5L);
        saved.setUserId(100L);
        OrderResponseDto orderResp = new OrderResponseDto(5L, 100L, "NEW", req.getCreationDate(), List.of());
        UserInfoResponseDto userResp = new UserInfoResponseDto(100L, "John", "Doe", LocalDate.of(1990, 1, 1), "john@example.com");

        when(orderMapper.toEntity(req)).thenReturn(entity);
        when(orderRepository.save(entity)).thenReturn(saved);
        when(orderMapper.toResponse(saved)).thenReturn(orderResp);
        when(userInfoService.getUserInfoById(100L)).thenReturn(userResp);

        OrderWithUserResponseDto result = orderService.createOrder(req);
        assertThat(result.getOrder().getId()).isEqualTo(5L);
        assertThat(result.getUser().getId()).isEqualTo(100L);
    }

    @Test
    void getOrderById_found() {
        Order order = new Order();
        order.setId(7L);
        order.setUserId(200L);
        OrderResponseDto orderResp = new OrderResponseDto(7L, 200L, "NEW", LocalDate.now(), List.of());
        UserInfoResponseDto userResp = new UserInfoResponseDto(200L, "A", "B", LocalDate.of(1990, 1, 1), "a@b.c");

        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResp);
        when(userInfoService.getUserInfoById(200L)).thenReturn(userResp);

        OrderWithUserResponseDto result = orderService.getOrderById(7L);
        assertThat(result.getOrder().getId()).isEqualTo(7L);
        assertThat(result.getUser().getId()).isEqualTo(200L);
    }

    @Test
    void getOrderById_notFound() {
        when(orderRepository.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.getOrderById(9L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Order with ID 9 not found");
    }

    @Test
    void getAllOrders_mapsUsers() {
        Order o1 = new Order();
        o1.setId(1L);
        o1.setUserId(11L);
        Order o2 = new Order();
        o2.setId(2L);
        o2.setUserId(22L);
        when(orderRepository.findAll()).thenReturn(List.of(o1, o2));
        when(orderMapper.toResponse(o1)).thenReturn(new OrderResponseDto(1L, 11L, "S", LocalDate.now(), List.of()));
        when(orderMapper.toResponse(o2)).thenReturn(new OrderResponseDto(2L, 22L, "S", LocalDate.now(), List.of()));
        when(userInfoService.getUserInfoById(11L)).thenReturn(new UserInfoResponseDto(11L, "n", "s", LocalDate.now(), "e1"));
        when(userInfoService.getUserInfoById(22L)).thenReturn(new UserInfoResponseDto(22L, "n", "s", LocalDate.now(), "e2"));

        var result = orderService.getAllOrders();
        assertThat(result).hasSize(2);
    }

    @Test
    void getOrdersByStatuses_filters() {
        Order o = new Order();
        o.setId(1L);
        o.setUserId(11L);
        when(orderRepository.findByStatuses(List.of("NEW", "PAID"))).thenReturn(List.of(o));
        when(orderMapper.toResponse(o)).thenReturn(new OrderResponseDto(1L, 11L, "NEW", LocalDate.now(), List.of()));
        when(userInfoService.getUserInfoById(11L)).thenReturn(new UserInfoResponseDto(11L, "n", "s", LocalDate.now(), "e"));

        var result = orderService.getOrdersByStatuses(List.of("NEW", "PAID"));
        assertThat(result).hasSize(1);
    }

    @Test
    void updateOrder_success() {
        OrderItemRequestDto itemReq = new OrderItemRequestDto(9L, 3);
        OrderRequestDto req = new OrderRequestDto(300L, "PAID", LocalDate.now(), List.of(itemReq));
        Order existing = new Order(); existing.setId(55L); existing.setUserId(999L);
        Order saved = new Order(); saved.setId(55L); saved.setUserId(300L);
        OrderResponseDto orderResp = new OrderResponseDto(55L, 300L, "PAID", req.getCreationDate(), List.of());
        UserInfoResponseDto userResp = new UserInfoResponseDto(300L, "N","S", LocalDate.now(), "e");

        when(orderRepository.findById(55L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(existing)).thenReturn(saved);
        when(orderMapper.toResponse(saved)).thenReturn(orderResp);
        when(userInfoService.getUserInfoById(300L)).thenReturn(userResp);
        when(orderItemMapper.toEntity(itemReq)).thenReturn(new by.osinovi.orderservice.entity.OrderItem());

        OrderWithUserResponseDto result = orderService.updateOrder(55L, req);
        assertThat(result.getOrder().getStatus()).isEqualTo("PAID");
        verify(orderRepository).save(existing);
        verify(orderItemMapper).toEntity(itemReq);
    }

    @Test
    void updateOrder_notFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.updateOrder(1L, new OrderRequestDto(1L, "S", LocalDate.now(), List.of())))
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
} 