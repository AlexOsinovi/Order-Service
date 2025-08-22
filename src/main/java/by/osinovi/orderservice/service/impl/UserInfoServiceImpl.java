package by.osinovi.orderservice.service.impl;

import by.osinovi.orderservice.dto.userInfo.UserInfoResponseDto;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {

    private final RestTemplate restTemplate;

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Override
    public UserInfoResponseDto getUserInfoById(Long userId) {
        String url = userServiceUrl + "/" + userId;
        try {
            return restTemplate.getForObject(url, UserInfoResponseDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("User with ID " + userId + " not found");
        } catch (Exception e) {
            throw new RuntimeException("Error getting user information by ID: " + userId, e);
        }
    }

    @Override
    public UserInfoResponseDto getUserInfoByEmail(String email) {
        String url = userServiceUrl + "email/" + email;
        try {
            return restTemplate.getForObject(url, UserInfoResponseDto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("User with email " + email + " not found");
        } catch (Exception e) {
            throw new RuntimeException("Error getting user information by email: " + email, e);
        }
    }
}