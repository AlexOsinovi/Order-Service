package by.osinovi.orderservice.mapper;

import by.osinovi.orderservice.dto.orderItem.OrderItemRequestDto;
import by.osinovi.orderservice.dto.orderItem.OrderItemResponseDto;
import by.osinovi.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ItemMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    @Mapping(source = "itemId", target = "item.id")
    OrderItem toEntity(OrderItemRequestDto orderItemRequest);

    OrderItemResponseDto toResponse(OrderItem orderItem);
}