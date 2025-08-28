package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.dto.item.ItemRequestDto;
import by.osinovi.orderservice.dto.item.ItemResponseDto;
import by.osinovi.orderservice.entity.Item;
import by.osinovi.orderservice.mapper.ItemMapper;
import by.osinovi.orderservice.repository.ItemRepository;
import by.osinovi.orderservice.service.ItemService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemMapper itemMapper;

    @Override
    public ItemResponseDto createItem(ItemRequestDto itemRequestDto) {
        Item item = itemMapper.toEntity(itemRequestDto);
        Item saved = itemRepository.save(item);
        return itemMapper.toResponse(saved);
    }

    @Override
    public ItemResponseDto getItemById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        return itemMapper.toResponse(item);
    }

    @Override
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto) {
        Item existing = itemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
        existing.setName(itemRequestDto.getName());
        existing.setPrice(itemRequestDto.getPrice());
        Item updated = itemRepository.save(existing);
        return itemMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
}