package by.osinovi.orderservice.controller;

import by.osinovi.orderservice.dto.item.ItemRequestDto;
import by.osinovi.orderservice.dto.item.ItemResponseDto;
import by.osinovi.orderservice.service.ItemService;
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
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(@Valid @RequestBody ItemRequestDto itemRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(itemRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getItemById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getItemById(id));
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.getAllItems());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long id, @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(itemService.updateItem(id, itemRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}