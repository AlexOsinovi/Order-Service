package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.item.ItemRequestDto;
import by.osinovi.orderservice.dto.item.ItemResponseDto;
import java.util.List;

public interface ItemService {
    ItemResponseDto createItem(ItemRequestDto itemRequestDto);
    ItemResponseDto getItemById(Long id);
    List<ItemResponseDto> getAllItems();
    ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto);
    void deleteItem(Long id);
}