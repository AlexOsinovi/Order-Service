package by.osinovi.orderservice.dto.order;

import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithUserResponseDto {
    private OrderResponseDto order;
    private UserInfoResponseDto user;
}