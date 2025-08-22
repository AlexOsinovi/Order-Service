package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;

public interface UserInfoService {
    UserInfoResponseDto getUserInfoById(Long userId);
    UserInfoResponseDto getUserInfoByEmail(String email);
}