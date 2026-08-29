package br.com.devl.mfc.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.devl.mfc.auth.entity.User;


public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmailIgnoreCase(String email);
	boolean existsByEmailIgnoreCase(String email);
}
