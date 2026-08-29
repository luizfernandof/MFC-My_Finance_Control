package br.com.devl.mfc.auth.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.devl.mfc.auth.entity.RefreshToken;
import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.auth.repository.RefreshTokenRepository;
import br.com.devl.mfc.exception.BusinessException;

@Service
public class RefreshTokenService {

	@Value("${jwt.refresh-expiration}")
	private Long refreshTokenDurationMs;
	
	private final RefreshTokenRepository refreshTokenRepository;
	
	public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
	}
	
	@Transactional
	public RefreshToken createRefreshToken(User user) {
		
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken.setExpiryDate(
				Instant.now().plusMillis(refreshTokenDurationMs)
		);
		
		return refreshTokenRepository.save(refreshToken);	
	}
	
	public RefreshToken verifyExpiration(RefreshToken token) {
		if(token.getExpiryDate().isBefore(Instant.now())) {
			refreshTokenRepository.delete(token);
			throw new BusinessException("REFRESH_TOKEN_EXPIRED", "Sessão expirada. Faça login novamente.",
					HttpStatus.UNAUTHORIZED);
		}
		if (!token.getUser().isEnabled()) {
			refreshTokenRepository.delete(token);
			throw new BusinessException("USER_DISABLED", "Esta conta está desabilitada.", HttpStatus.FORBIDDEN);
		}

		return token;
	}

	@Transactional
	public RefreshToken rotateRefreshToken(String tokenValue) {
		RefreshToken current = refreshTokenRepository.findByToken(tokenValue)
				.orElseThrow(() -> new BusinessException("INVALID_REFRESH_TOKEN",
						"Sessão inválida. Faça login novamente.", HttpStatus.UNAUTHORIZED));
		verifyExpiration(current);
		User user = current.getUser();
		refreshTokenRepository.delete(current);
		refreshTokenRepository.flush();
		return createRefreshToken(user);
	}

	@Transactional
	public void deleteByToken(String tokenValue) {
		refreshTokenRepository.findByToken(tokenValue).ifPresent(refreshTokenRepository::delete);
	}
}
