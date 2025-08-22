package by.osinovi.orderservice.controller;

import by.osinovi.orderservice.dto.order_item.OrderItemRequestDto;
import by.osinovi.orderservice.dto.order_item.OrderItemResponseDto;
import by.osinovi.orderservice.service.OrderItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
public class OrderItemController {
    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<OrderItemResponseDto> createOrderItem(@RequestParam Long orderId, @Valid @RequestBody OrderItemRequestDto orderItemRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderItemService.createOrderItem(orderItemRequestDto, orderId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderItemResponseDto> getOrderItemById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderItemService.getOrderItemById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemResponseDto>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(orderItemService.getOrderItemsByOrderId(orderId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderItemResponseDto> updateOrderItem(@PathVariable Long id, @Valid @RequestBody OrderItemRequestDto orderItemRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(orderItemService.updateOrderItem(id, orderItemRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}