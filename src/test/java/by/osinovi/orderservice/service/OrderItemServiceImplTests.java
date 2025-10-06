package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.order_item.OrderItemRequestDto;
import by.osinovi.orderservice.dto.order_item.OrderItemResponseDto;
import by.osinovi.orderservice.entity.Item;
import by.osinovi.orderservice.entity.Order;
import by.osinovi.orderservice.entity.OrderItem;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.mapper.OrderItemMapper;
import by.osinovi.orderservice.repository.ItemRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderItemServiceImplTests {

	@Mock
	private OrderItemRepository orderItemRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private ItemRepository itemRepository; // <-- ДОБАВЛЕН МОК

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
		Item item = new Item();
		item.setId(3L);
		OrderItem entity = new OrderItem();
		entity.setItem(item);
		entity.setQuantity(2);
		OrderItem saved = new OrderItem();
		saved.setId(11L);
		OrderItemResponseDto resp = new OrderItemResponseDto(11L, null, 2);

		when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
		when(itemRepository.findById(3L)).thenReturn(Optional.of(item)); // <-- ИСПРАВЛЕНО: Добавлен мок для itemRepository
		when(orderItemMapper.toEntity(req)).thenReturn(entity);
		when(orderItemRepository.save(any(OrderItem.class))).thenReturn(saved);
		when(orderItemMapper.toResponse(saved)).thenReturn(resp);

		OrderItemResponseDto result = orderItemService.createOrderItem(req, 7L);

		assertThat(result.getId()).isEqualTo(11L);
		ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);
		verify(orderItemRepository).save(captor.capture());
		assertThat(captor.getValue().getOrder().getId()).isEqualTo(7L);
		assertThat(captor.getValue().getItem().getId()).isEqualTo(3L);
	}

	@Test
	void createOrderItem_orderNotFound() {
		when(orderRepository.findById(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> orderItemService.createOrderItem(new OrderItemRequestDto(3L, 1), 1L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Order with ID 1 not found");
	}

	@Test
	void createOrderItem_itemNotFound() {
		Order order = new Order();
		order.setId(7L);
		OrderItem entity = new OrderItem();
		Item item = new Item();
		item.setId(3L);
		entity.setItem(item);

		when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
		when(orderItemMapper.toEntity(any(OrderItemRequestDto.class))).thenReturn(entity);
		when(itemRepository.findById(3L)).thenReturn(Optional.empty()); // Мок для несуществующего товара

		assertThatThrownBy(() -> orderItemService.createOrderItem(new OrderItemRequestDto(3L, 1), 7L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Item with ID 3 not found");
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
		OrderItemRequestDto req = new OrderItemRequestDto(99L, 5); // Новый ID товара и количество
		Item existingItem = new Item();
		existingItem.setId(1L);
		Item newItem = new Item();
		newItem.setId(99L);
		OrderItem existingOrderItem = new OrderItem();
		existingOrderItem.setId(12L);
		existingOrderItem.setItem(existingItem);
		existingOrderItem.setQuantity(1);

		when(orderItemRepository.findById(12L)).thenReturn(Optional.of(existingOrderItem));
		when(itemRepository.findById(99L)).thenReturn(Optional.of(newItem)); // <-- ИСПРАВЛЕНО: Добавлен мок для нового товара
		when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Возвращаем измененный объект
		when(orderItemMapper.toResponse(any(OrderItem.class))).thenAnswer(invocation -> {
			OrderItem saved = invocation.getArgument(0);
			return new OrderItemResponseDto(saved.getId(), null, saved.getQuantity());
		});

		orderItemService.updateOrderItem(12L, req);

		ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);
		verify(orderItemRepository).save(captor.capture());
		assertThat(captor.getValue().getId()).isEqualTo(12L);
		assertThat(captor.getValue().getQuantity()).isEqualTo(5);
		assertThat(captor.getValue().getItem().getId()).isEqualTo(99L); // Проверяем, что товар изменился
	}

	@Test
	void updateOrderItem_success_changesOnlyQuantity() {
		OrderItemRequestDto req = new OrderItemRequestDto(1L, 10); // ID товара тот же, меняем количество
		Item item = new Item();
		item.setId(1L);
		OrderItem existingOrderItem = new OrderItem();
		existingOrderItem.setId(15L);
		existingOrderItem.setItem(item);
		existingOrderItem.setQuantity(5);

		when(orderItemRepository.findById(15L)).thenReturn(Optional.of(existingOrderItem));

		orderItemService.updateOrderItem(15L, req);

		verify(itemRepository, never()).findById(any(Long.class)); // Убеждаемся, что itemRepository не вызывался
		ArgumentCaptor<OrderItem> captor = ArgumentCaptor.forClass(OrderItem.class);
		verify(orderItemRepository).save(captor.capture());
		assertThat(captor.getValue().getQuantity()).isEqualTo(10);
		assertThat(captor.getValue().getItem().getId()).isEqualTo(1L);
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