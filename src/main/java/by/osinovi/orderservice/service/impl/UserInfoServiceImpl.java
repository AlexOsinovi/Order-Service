package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.client.UserClient;
import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.service.UserInfoService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

    private final UserClient userClient;

    @Override
    public UserInfoResponseDto getUserInfoById(Long userId) {
        try {
            return userClient.getUserInfoById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("User with ID " + userId + " not found");
        } catch (Exception e) {
            throw new RuntimeException("Error getting user by ID: " + userId, e);
        }
    }

    @Override
    public UserInfoResponseDto getUserInfoByEmail(String email) {
        try {
            return userClient.getUserInfoByEmail(email);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("User with email " + email + " not found");
        } catch (Exception e) {
            throw new RuntimeException("Error getting user by email: " + email, e);
        }
    }
}