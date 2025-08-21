package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.orderItem.OrderItemRequestDto;
import by.osinovi.orderservice.dto.orderItem.OrderItemResponseDto;
import by.osinovi.orderservice.entity.Item;
import by.osinovi.orderservice.entity.Order;
import by.osinovi.orderservice.entity.OrderItem;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.mapper.OrderItemMapper;
import by.osinovi.orderservice.repository.OrderItemRepository;
import by.osinovi.orderservice.repository.OrderRepository;
import by.osinovi.orderservice.service.impl.OrderItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class OrderItemServiceImplTests {

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderItemMapper orderItemMapper;

	@InjectMocks
	private OrderItemServiceImpl orderItemService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void createOrderItem_success() {
		OrderItemRequestDto req = new OrderItemRequestDto(3L, 2);
		Order order = new Order();
		order.setId(7L);
		OrderItem entity = new OrderItem();
		entity.setItem(new Item(3L));
		entity.setQuantity(2);
		OrderItem saved = new OrderItem();
		saved.setId(11L);
		OrderItemResponseDto resp = new OrderItemResponseDto(11L, null, 2);

		when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
		when(orderItemMapper.toEntity(req)).thenReturn(entity);
		when(orderItemRepository.save(entity)).thenReturn(saved);
		when(orderItemMapper.toResponse(saved)).thenReturn(resp);

		OrderItemResponseDto result = orderItemService.createOrderItem(req, 7L);
		assertThat(result.getId()).isEqualTo(11L);
		ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);
		verify(orderItemRepository).save(captor.capture());
		assertThat(captor.getValue().getOrder().getId()).isEqualTo(7L);
	}

	@Test
	void createOrderItem_orderNotFound() {
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> orderItemService.createOrderItem(new OrderItemRequestDto(3L, 1), 1L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Order with ID 1 not found");
	}

	@Test
	void getOrderItemById_found() {
		OrderItem item = new OrderItem();
		item.setId(5L);
		OrderItemResponseDto resp = new OrderItemResponseDto(5L, null, 1);
		when(orderItemRepository.findById(5L)).thenReturn(Optional.of(item));
		when(orderItemMapper.toResponse(item)).thenReturn(resp);
		OrderItemResponseDto result = orderItemService.getOrderItemById(5L);
		assertThat(result.getId()).isEqualTo(5L);
	}

	@Test
	void getOrderItemById_notFound() {
		when(orderItemRepository.findById(5L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> orderItemService.getOrderItemById(5L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Order item with ID 5 not found");
	}

	@Test
	void getOrderItemsByOrderId_maps() {
		OrderItem oi1 = new OrderItem();
		oi1.setId(1L);
		OrderItem oi2 = new OrderItem();
		oi2.setId(2L);
		when(orderItemRepository.findByOrderId(9L)).thenReturn(List.of(oi1, oi2));
		when(orderItemMapper.toResponse(oi1)).thenReturn(new OrderItemResponseDto(1L, null, 1));
		when(orderItemMapper.toResponse(oi2)).thenReturn(new OrderItemResponseDto(2L, null, 2));
		List<OrderItemResponseDto> result = orderItemService.getOrderItemsByOrderId(9L);
		assertThat(result).hasSize(2);
	}

	@Test
	void updateOrderItem_success_changesQuantityAndItem() {
		OrderItemRequestDto req = new OrderItemRequestDto(99L, 5);
		OrderItem existing = new OrderItem();
		existing.setId(12L);
		existing.setItem(new Item(1L));
		existing.setQuantity(1);
		OrderItem saved = new OrderItem();
		saved.setId(12L);
		saved.setItem(new Item(99L));
		saved.setQuantity(5);
		OrderItemResponseDto resp = new OrderItemResponseDto(12L, null, 5);

		when(orderItemRepository.findById(12L)).thenReturn(Optional.of(existing));
		when(orderItemRepository.save(existing)).thenReturn(saved);
		when(orderItemMapper.toResponse(saved)).thenReturn(resp);

		OrderItemResponseDto result = orderItemService.updateOrderItem(12L, req);
		assertThat(result.getId()).isEqualTo(12L);
		assertThat(result.getQuantity()).isEqualTo(5);
	}

	@Test
	void updateOrderItem_notFound() {
		when(orderItemRepository.findById(77L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> orderItemService.updateOrderItem(77L, new OrderItemRequestDto(1L, 1)))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Order item with ID 77 not found");
	}

	@Test
	void deleteOrderItem_success() {
		when(orderItemRepository.existsById(8L)).thenReturn(true);
		orderItemService.deleteOrderItem(8L);
		verify(orderItemRepository).deleteById(8L);
	}

	@Test
	void deleteOrderItem_notFound() {
		when(orderItemRepository.existsById(8L)).thenReturn(false);
		assertThatThrownBy(() -> orderItemService.deleteOrderItem(8L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Order item with ID 8 not found");
	}
} 