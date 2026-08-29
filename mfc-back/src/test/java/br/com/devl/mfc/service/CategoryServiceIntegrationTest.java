package br.com.devl.mfc.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.auth.enums.UserRole;
import br.com.devl.mfc.auth.repository.UserRepository;
import br.com.devl.mfc.dto.CategoryResponseDTO;
import br.com.devl.mfc.entity.Category;
import br.com.devl.mfc.enums.CategoryType;
import br.com.devl.mfc.repository.CategoryRepository;

@SpringBootTest
@ActiveProfiles("test")
class CategoryServiceIntegrationTest {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Test
	void searchesAndPaginatesCategoriesWithinTheAuthenticatedUser() {
		User user = saveUser("category-owner-");
		User otherUser = saveUser("category-other-");
		saveCategory(user, "Alimentação");
		saveCategory(user, "Lazer");
		saveCategory(user, "Moradia");
		saveCategory(otherUser, "Alimentação confidencial");

		var firstPage = categoryService.list(user, "  A  ",
				PageRequest.of(0, 2, Sort.by("name").ascending()));
		var secondPage = categoryService.list(user, "a",
				PageRequest.of(1, 2, Sort.by("name").ascending()));
		var specificMatch = categoryService.list(user, "MOR",
				PageRequest.of(0, 10, Sort.by("name").ascending()));

		assertThat(firstPage.getTotalElements()).isEqualTo(3);
		assertThat(firstPage.getTotalPages()).isEqualTo(2);
		assertThat(firstPage.getContent()).extracting(CategoryResponseDTO::name)
				.containsExactly("Alimentação", "Lazer");
		assertThat(secondPage.getContent()).extracting(CategoryResponseDTO::name)
				.containsExactly("Moradia");
		assertThat(specificMatch.getContent()).extracting(CategoryResponseDTO::name)
				.containsExactly("Moradia");
	}

	@Test
	void returnsAllCategoryOptionsSortedAndScopedToTheUser() {
		User user = saveUser("options-owner-");
		User otherUser = saveUser("options-other-");
		saveCategory(user, "Viagens");
		saveCategory(user, "Alimentação");
		saveCategory(otherUser, "Categoria privada");

		assertThat(categoryService.listOptions(user)).extracting(CategoryResponseDTO::name)
				.containsExactly("Alimentação", "Viagens");
	}

	private User saveUser(String prefix) {
		User user = new User();
		user.setEmail(prefix + UUID.randomUUID() + "@mfc.local");
		user.setPassword("encoded-password");
		user.setRole(UserRole.USER);
		user.setEnabled(true);
		return userRepository.save(user);
	}

	private void saveCategory(User user, String name) {
		Category category = new Category();
		category.setName(name);
		category.setType(CategoryType.EXPENSE);
		category.setUser(user);
		categoryRepository.save(category);
	}
}
