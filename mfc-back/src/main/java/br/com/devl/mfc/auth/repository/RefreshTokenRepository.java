package br.com.devl.mfc.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.devl.mfc.auth.entity.RefreshToken;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	
	Optional<RefreshToken> findByToken(String token);
}
