package by.osinovi.orderservice.dto.user_info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDto {
    private Long id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}