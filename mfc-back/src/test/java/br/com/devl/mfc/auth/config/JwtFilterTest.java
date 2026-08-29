package br.com.devl.mfc.auth.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import br.com.devl.mfc.auth.exception.SecurityExceptionHandler;
import br.com.devl.mfc.auth.repository.UserRepository;
import br.com.devl.mfc.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;

class JwtFilterTest {

	private final JwtFilter jwtFilter = new JwtFilter(
			mock(JwtService.class),
			mock(UserRepository.class),
			mock(SecurityExceptionHandler.class));

	@Test
	void shouldSkipJwtValidationForHealthEndpoint() {
		assertTrue(jwtFilter.shouldNotFilter(requestFor("/actuator/health")));
		assertTrue(jwtFilter.shouldNotFilter(requestFor("/actuator/health/liveness")));
	}

	@Test
	void shouldRequireJwtValidationForProtectedEndpoint() {
		assertFalse(jwtFilter.shouldNotFilter(requestFor("/transactions")));
	}

	private HttpServletRequest requestFor(String path) {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getServletPath()).thenReturn(path);
		return request;
	}
}
