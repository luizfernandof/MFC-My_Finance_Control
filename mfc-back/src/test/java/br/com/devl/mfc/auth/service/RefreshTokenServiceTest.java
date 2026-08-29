package br.com.devl.mfc.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.devl.mfc.auth.entity.RefreshToken;
import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.auth.repository.RefreshTokenRepository;
import br.com.devl.mfc.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	@Mock
	private RefreshTokenRepository repository;

	private RefreshTokenService service;

	@BeforeEach
	void setUp() {
		service = new RefreshTokenService(repository);
		ReflectionTestUtils.setField(service, "refreshTokenDurationMs", 60_000L);
	}

	@Test
	void rotatesAValidTokenAndInvalidatesThePreviousOne() {
		User user = enabledUser();
		RefreshToken current = token("old-token", user, Instant.now().plusSeconds(60));
		when(repository.findByToken("old-token")).thenReturn(Optional.of(current));
		when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		RefreshToken rotated = service.rotateRefreshToken("old-token");

		verify(repository).delete(current);
		verify(repository).flush();
		assertThat(rotated.getUser()).isSameAs(user);
		assertThat(rotated.getToken()).isNotBlank().isNotEqualTo("old-token");
		assertThat(rotated.getExpiryDate()).isAfter(Instant.now());
	}

	@Test
	void rejectsAndDeletesAnExpiredToken() {
		RefreshToken expired = token("expired", enabledUser(), Instant.now().minusSeconds(1));

		assertThatThrownBy(() -> service.verifyExpiration(expired))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("Sessão expirada");
		verify(repository).delete(expired);
	}

	private User enabledUser() {
		User user = new User();
		user.setEnabled(true);
		return user;
	}

	private RefreshToken token(String value, User user, Instant expiry) {
		RefreshToken token = new RefreshToken();
		token.setToken(value);
		token.setUser(user);
		token.setExpiryDate(expiry);
		return token;
	}
}
