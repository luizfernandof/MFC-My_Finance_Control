package br.com.devl.mfc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	
	List<Category> findByUserOrderByNameAsc(User user);

	Page<Category> findByUser(User user, Pageable pageable);

	Page<Category> findByUserAndNameContainingIgnoreCase(User user, String name, Pageable pageable);
	
	Optional<Category> findByIdAndUser(Long id, User user);
	
	boolean existsByNameIgnoreCaseAndUser(String name, User user);

	boolean existsByNameIgnoreCaseAndUserAndIdNot(String name, User user, Long id);

}
