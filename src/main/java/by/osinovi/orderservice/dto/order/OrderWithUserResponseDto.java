package by.osinovi.orderservice.dto.order;

import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithUserResponseDto {
    private OrderResponseDto order;
    private UserInfoResponseDto user;
}