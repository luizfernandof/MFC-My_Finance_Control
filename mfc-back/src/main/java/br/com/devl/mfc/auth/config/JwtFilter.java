package br.com.devl.mfc.auth.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.auth.repository.UserRepository;
import br.com.devl.mfc.auth.service.JwtService;
import br.com.devl.mfc.auth.exception.SecurityExceptionHandler;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final SecurityExceptionHandler securityExceptionHandler;

	public JwtFilter(JwtService jwtService, UserRepository userRepository,
			SecurityExceptionHandler securityExceptionHandler) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.securityExceptionHandler = securityExceptionHandler;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return path.startsWith("/auth/")
		        || path.equals("/actuator/health")
		        || path.startsWith("/actuator/health/")
		        || path.startsWith("/h2-console")
		        || path.startsWith("/swagger-ui")
		        || path.startsWith("/v3/api-docs");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);

			try {
				String email = jwtService.getEmail(token);
					User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

					if (user != null && user.isEnabled()) {
						UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
								user.getAuthorities());
					SecurityContextHolder.getContext().setAuthentication(auth);
					filterChain.doFilter(request, response);
					return;
					}
					securityExceptionHandler.writeUnauthorized(response, "USER_UNAVAILABLE",
							"Usuário inexistente ou desabilitado.");
					return;
				} catch (ExpiredJwtException e) {
					securityExceptionHandler.writeUnauthorized(response, "ACCESS_TOKEN_EXPIRED", "Token expirado.");
					return;
				} catch (JwtException e) {
					securityExceptionHandler.writeUnauthorized(response, "INVALID_ACCESS_TOKEN", "Token inválido.");
					return;
				}
			}

			securityExceptionHandler.writeUnauthorized(response, "ACCESS_TOKEN_MISSING", "Token não fornecido.");
	}

}
