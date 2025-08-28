package by.osinovi.orderservice.client;

import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {
    @GetMapping("/{id}")
    UserInfoResponseDto getUserInfoById(@PathVariable("id") Long id);

    @GetMapping("/email/{email}")
    UserInfoResponseDto getUserInfoByEmail(@PathVariable("email") String email);
}