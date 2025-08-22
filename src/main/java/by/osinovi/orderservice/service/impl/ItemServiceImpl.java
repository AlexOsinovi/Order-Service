package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.dto.item.ItemRequestDto;
import by.osinovi.orderservice.dto.item.ItemResponseDto;
import by.osinovi.orderservice.entity.Item;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.mapper.ItemMapper;
import by.osinovi.orderservice.repository.ItemRepository;
import by.osinovi.orderservice.service.ItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemResponseDto createItem(ItemRequestDto itemRequestDto) {
        Item item = itemMapper.toEntity(itemRequestDto);
        Item saved = itemRepository.save(item);
        return itemMapper.toResponse(saved);
    }

    @Override
    public ItemResponseDto getItemById(Long id) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Item with ID " + id + " not found"));
        return itemMapper.toResponse(item);
    }

    @Override
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto) {
        Item existing = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Item with ID " + id + " not found"));
        existing.setName(itemRequestDto.getName());
        existing.setPrice(itemRequestDto.getPrice());
        Item updated = itemRepository.save(existing);
        return itemMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new NotFoundException("Item with ID " + id + " not found");
        }
        itemRepository.deleteById(id);
    }
}