package by.osinovi.orderservice.service;

import by.osinovi.orderservice.client.UserClient;
import by.osinovi.orderservice.dto.user_info.UserInfoResponseDto;
import by.osinovi.orderservice.exception.NotFoundException;
import by.osinovi.orderservice.service.impl.UserInfoServiceImpl;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class UserInfoServiceImplTests {

	@Mock
	private UserClient userClient;

	@InjectMocks
	private UserInfoServiceImpl service;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void getUserInfoById_success() {
		UserInfoResponseDto dto = new UserInfoResponseDto(1L, "N", "S", null, "e");
		when(userClient.getUserInfoById(1L)).thenReturn(dto);
		var result = service.getUserInfoById(1L);
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getUserInfoById_notFound() {
		when(userClient.getUserInfoById(2L)).thenThrow(FeignException.NotFound.class);
		assertThatThrownBy(() -> service.getUserInfoById(2L))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("User with ID 2 not found");
	}

	@Test
	void getUserInfoById_runtime() {
		when(userClient.getUserInfoById(3L)).thenThrow(new RuntimeException("boom"));
		assertThatThrownBy(() -> service.getUserInfoById(3L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Error getting user by ID: 3");
	}

	@Test
	void getUserInfoByEmail_success() {
		UserInfoResponseDto dto = new UserInfoResponseDto(1L, "N", "S", null, "a@b.c");
		when(userClient.getUserInfoByEmail("a@b.c")).thenReturn(dto);
		var result = service.getUserInfoByEmail("a@b.c");
		assertThat(result.getEmail()).isEqualTo("a@b.c");
	}

	@Test
	void getUserInfoByEmail_notFound() {
		when(userClient.getUserInfoByEmail("x@b.c")).thenThrow(FeignException.NotFound.class);
		assertThatThrownBy(() -> service.getUserInfoByEmail("x@b.c"))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining("User with email x@b.c not found");
	}

	@Test
	void getUserInfoByEmail_runtime() {
		when(userClient.getUserInfoByEmail("z@b.c")).thenThrow(new RuntimeException("boom"));
		assertThatThrownBy(() -> service.getUserInfoByEmail("z@b.c"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Error getting user by email: z@b.c");
	}
}