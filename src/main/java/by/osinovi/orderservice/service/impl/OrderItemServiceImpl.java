package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.dto.order_item.OrderItemRequestDto;
import by.osinovi.orderservice.dto.order_item.OrderItemResponseDto;
import by.osinovi.orderservice.entity.Order;
import by.osinovi.orderservice.entity.OrderItem;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.mapper.OrderItemMapper;
import by.osinovi.orderservice.repository.ItemRepository;
import by.osinovi.orderservice.repository.OrderItemRepository;
import by.osinovi.orderservice.repository.OrderRepository;
import by.osinovi.orderservice.service.OrderItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemMapper orderItemMapper;

    @Override
    public OrderItemResponseDto createOrderItem(OrderItemRequestDto orderItemRequestDto, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order with ID " + orderId + " not found"));
        OrderItem orderItem = orderItemMapper.toEntity(orderItemRequestDto);
        orderItem.setOrder(order);
        orderItem.setItem(itemRepository.findById(orderItem.getItem().getId()).orElseThrow(() -> new NotFoundException("Item with ID " + orderItem.getItem().getId() + " not found")));
        OrderItem saved = orderItemRepository.save(orderItem);
        return orderItemMapper.toResponse(saved);
    }

    @Override
    public OrderItemResponseDto getOrderItemById(Long id) {
        OrderItem orderItem = orderItemRepository.findById(id).orElseThrow(() -> new NotFoundException("Order item with ID " + id + " not found"));
        return orderItemMapper.toResponse(orderItem);
    }

    @Override
    public List<OrderItemResponseDto> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OrderItemResponseDto updateOrderItem(Long id, OrderItemRequestDto orderItemRequestDto) {
        OrderItem existing = orderItemRepository.findById(id).orElseThrow(() -> new NotFoundException("Order item with ID " + id + " not found"));
        existing.setQuantity(orderItemRequestDto.getQuantity());
        if (!existing.getItem().getId().equals(orderItemRequestDto.getItemId())) {
            existing.setItem(existing.getItem());
        }
        OrderItem updated = orderItemRepository.save(existing);
        return orderItemMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteOrderItem(Long id) {
        if (!orderItemRepository.existsById(id)) {
            throw new NotFoundException("Order item with ID " + id + " not found");
        }
        orderItemRepository.deleteById(id);
    }
}