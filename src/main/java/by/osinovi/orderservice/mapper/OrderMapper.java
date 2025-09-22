package by.osinovi.orderservice.mapper;

import by.osinovi.orderservice.dto.message.OrderMessage;
import by.osinovi.orderservice.dto.order.OrderRequestDto;
import by.osinovi.orderservice.dto.order.OrderResponseDto;
import by.osinovi.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {OrderItemMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    Order toEntity(OrderRequestDto orderRequest);


    OrderResponseDto toResponse(Order order);

    @Mapping(target = "orderId", source = "order.id")
    OrderMessage toMessage(Order order, BigDecimal totalAmount);
}