package by.osinovi.orderservice.controller;

import by.osinovi.orderservice.document.OrderDocument;
import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderWithUserResponseDto;
import by.osinovi.orderservice.service.OrderCommandService;
import by.osinovi.orderservice.service.OrderQueryService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    @PostMapping
    public ResponseEntity<OrderWithUserResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderCommandService.createOrder(orderRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDocument> getOrderById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderQueryService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderDocument>> getAllOrders() {
        return ResponseEntity.status(HttpStatus.OK).body(orderQueryService.getAllOrders());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderWithUserResponseDto> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequestDto orderRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(orderCommandService.updateOrder(id, orderRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderCommandService.deleteOrder(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}