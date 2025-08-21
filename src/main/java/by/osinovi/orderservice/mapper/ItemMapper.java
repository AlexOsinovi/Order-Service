package by.osinovi.orderservice.mapper;

import by.osinovi.orderservice.dto.item.ItemRequestDto;
import by.osinovi.orderservice.dto.item.ItemResponseDto;
import by.osinovi.orderservice.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {

    Item toEntity(ItemRequestDto itemRequest);
    ItemResponseDto toResponse(Item item);
}