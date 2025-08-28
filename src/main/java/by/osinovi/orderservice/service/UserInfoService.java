package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.userInfo.UserInfoResponseDto;

public interface UserInfoService {
    UserInfoResponseDto getUserInfoById(Long userId);
    UserInfoResponseDto getUserInfoByEmail(String email);
}