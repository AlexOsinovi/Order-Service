package by.osinovi.orderservice.service;

import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.service.impl.UserInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserInfoServiceImplTests {

	@Mock
	private RestTemplate restTemplate;

	@InjectMocks
	private UserInfoServiceImpl service;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		var urlField = UserInfoServiceImpl.class.getDeclaredField("userServiceUrl");
		urlField.setAccessible(true);
		urlField.set(service, "http://user-service");
	}

	@Test
	void getUserInfoById_success() {
		UserInfoResponseDto dto = new UserInfoResponseDto(1L, "N","S", null, "e");
		when(restTemplate.getForObject("http://user-service/1", UserInfoResponseDto.class)).thenReturn(dto);
		var result = service.getUserInfoById(1L);
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getUserInfoById_notFound() {
		when(restTemplate.getForObject("http://user-service/2", UserInfoResponseDto.class))
				.thenThrow(HttpClientErrorException.NotFound.class);
		assertThatThrownBy(() -> service.getUserInfoById(2L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("User with ID 2 not found");
	}

	@Test
	void getUserInfoById_runtime() {
		when(restTemplate.getForObject("http://user-service/3", UserInfoResponseDto.class))
				.thenThrow(new RuntimeException("boom"));
		assertThatThrownBy(() -> service.getUserInfoById(3L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Error getting user information by ID: 3");
	}

	@Test
	void getUserInfoByEmail_success() {
		UserInfoResponseDto dto = new UserInfoResponseDto(1L, "N","S", null, "a@b.c");
		when(restTemplate.getForObject("http://user-serviceemail/a@b.c", UserInfoResponseDto.class)).thenReturn(dto);
		var result = service.getUserInfoByEmail("a@b.c");
		assertThat(result.getEmail()).isEqualTo("a@b.c");
	}

	@Test
	void getUserInfoByEmail_notFound() {
		when(restTemplate.getForObject("http://user-serviceemail/x@b.c", UserInfoResponseDto.class))
				.thenThrow(HttpClientErrorException.NotFound.class);
		assertThatThrownBy(() -> service.getUserInfoByEmail("x@b.c"))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("User with email x@b.c not found");
	}

	@Test
	void getUserInfoByEmail_runtime() {
		when(restTemplate.getForObject("http://user-serviceemail/z@b.c", UserInfoResponseDto.class))
				.thenThrow(new RuntimeException("boom"));
		assertThatThrownBy(() -> service.getUserInfoByEmail("z@b.c"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Error getting user information by email: z@b.c");
	}
} 