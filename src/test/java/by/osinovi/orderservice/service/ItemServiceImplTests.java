package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.item.ItemRequestDto;
import by.osinovi.orderservice.dto.item.ItemResponseDto;
import by.osinovi.orderservice.entity.Item;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.mapper.ItemMapper;
import by.osinovi.orderservice.repository.ItemRepository;
import by.osinovi.orderservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ItemServiceImplTests {

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private ItemMapper itemMapper;

	@InjectMocks
	private ItemServiceImpl itemService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void createItem_success() {
		ItemRequestDto request = new ItemRequestDto("Book", new BigDecimal("12.50"));
		Item entity = new Item(null, "Book", new BigDecimal("12.50"));
		Item saved = new Item(1L, "Book", new BigDecimal("12.50"));
		ItemResponseDto response = new ItemResponseDto(1L, "Book", new BigDecimal("12.50"));

		when(itemMapper.toEntity(request)).thenReturn(entity);
		when(itemRepository.save(entity)).thenReturn(saved);
		when(itemMapper.toResponse(saved)).thenReturn(response);

		ItemResponseDto result = itemService.createItem(request);

		assertThat(result).isEqualTo(response);
		verify(itemRepository).save(entity);
	}

	@Test
	void getItemById_found() {
		Item item = new Item(2L, "Pen", new BigDecimal("2.00"));
		ItemResponseDto response = new ItemResponseDto(2L, "Pen", new BigDecimal("2.00"));
		when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
		when(itemMapper.toResponse(item)).thenReturn(response);

		ItemResponseDto result = itemService.getItemById(2L);
		assertThat(result).isEqualTo(response);
	}

	@Test
	void getItemById_notFound() {
		when(itemRepository.findById(99L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> itemService.getItemById(99L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Item with ID 99 not found");
	}

	@Test
	void getAllItems_mapsAll() {
		Item i1 = new Item(1L, "A", new BigDecimal("1.00"));
		Item i2 = new Item(2L, "B", new BigDecimal("2.00"));
		when(itemRepository.findAll()).thenReturn(List.of(i1, i2));
		when(itemMapper.toResponse(i1)).thenReturn(new ItemResponseDto(1L, "A", new BigDecimal("1.00")));
		when(itemMapper.toResponse(i2)).thenReturn(new ItemResponseDto(2L, "B", new BigDecimal("2.00")));

		List<ItemResponseDto> result = itemService.getAllItems();
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getId()).isEqualTo(1L);
		assertThat(result.get(1).getId()).isEqualTo(2L);
	}

	@Test
	void updateItem_success() {
		ItemRequestDto request = new ItemRequestDto("NewName", new BigDecimal("5.00"));
		Item existing = new Item(5L, "Old", new BigDecimal("1.00"));
		Item updated = new Item(5L, "NewName", new BigDecimal("5.00"));
		ItemResponseDto response = new ItemResponseDto(5L, "NewName", new BigDecimal("5.00"));
		when(itemRepository.findById(5L)).thenReturn(Optional.of(existing));
		when(itemRepository.save(existing)).thenReturn(updated);
		when(itemMapper.toResponse(updated)).thenReturn(response);

		ItemResponseDto result = itemService.updateItem(5L, request);
		assertThat(result).isEqualTo(response);

		ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
		verify(itemRepository).save(captor.capture());
		assertThat(captor.getValue().getName()).isEqualTo("NewName");
		assertThat(captor.getValue().getPrice()).isEqualTo(new BigDecimal("5.00"));
	}

	@Test
	void updateItem_notFound() {
		when(itemRepository.findById(123L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> itemService.updateItem(123L, new ItemRequestDto("X", new BigDecimal("1.00"))))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Item with ID 123 not found");
	}

	@Test
	void deleteItem_success() {
		when(itemRepository.existsById(10L)).thenReturn(true);
		itemService.deleteItem(10L);
		verify(itemRepository).deleteById(10L);
	}

	@Test
	void deleteItem_notFound() {
		when(itemRepository.existsById(10L)).thenReturn(false);
		assertThatThrownBy(() -> itemService.deleteItem(10L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("Item with ID 10 not found");
	}
} 