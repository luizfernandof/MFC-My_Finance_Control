package br.com.devl.mfc.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import br.com.devl.mfc.dto.TransactionResponseDTO;
import br.com.devl.mfc.entity.Category;
import br.com.devl.mfc.entity.Transaction;
import br.com.devl.mfc.enums.CategoryType;
import br.com.devl.mfc.enums.TransactionType;
import br.com.devl.mfc.repository.CategoryRepository;
import br.com.devl.mfc.repository.TransactionRepository;

@SpringBootTest
@ActiveProfiles("test")
class TransactionServiceIntegrationTest {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Test
	void listsTransactionsWithTheirLazyCategoryAfterRepositoryReturns() {
		User user = new User();
		user.setEmail("integration-" + UUID.randomUUID() + "@mfc.local");
		user.setPassword("encoded-password");
		user.setRole(UserRole.USER);
		user.setEnabled(true);
		user = userRepository.save(user);

		Category category = new Category();
		category.setName("Moradia " + UUID.randomUUID());
		category.setType(CategoryType.EXPENSE);
		category.setUser(user);
		category = categoryRepository.save(category);

		Transaction transaction = new Transaction();
		transaction.setDescription("Aluguel");
		transaction.setAmount(new BigDecimal("1500.00"));
		transaction.setDate(LocalDate.of(2026, 1, 8));
		transaction.setType(TransactionType.EXPENSE);
		transaction.setCategory(category);
		transaction.setUser(user);
		transactionRepository.save(transaction);

		var page = transactionService.list(user, 1, 2026, "", PageRequest.of(0, 10));

		assertThat(page.getContent())
				.extracting(TransactionResponseDTO::categoryName)
				.containsExactly(category.getName());
	}

	@Test
	void searchesByDescriptionOrCategoryAndKeepsPaginationPeriodAndUserScope() {
		User user = saveUser("search-owner-");
		User otherUser = saveUser("search-other-");
		Category food = saveCategory(user, "Alimentação");
		Category housing = saveCategory(user, "Moradia");
		Category otherFood = saveCategory(otherUser, "Alimentação");

		saveTransaction(user, food, "Mercado semanal", LocalDate.of(2026, 1, 8));
		saveTransaction(user, food, "Mercado extra", LocalDate.of(2026, 1, 15));
		saveTransaction(user, housing, "Aluguel", LocalDate.of(2026, 1, 10));
		saveTransaction(user, food, "Mercado fevereiro", LocalDate.of(2026, 2, 3));
		saveTransaction(otherUser, otherFood, "Mercado de outro usuário", LocalDate.of(2026, 1, 5));

		var firstPage = transactionService.list(user, 1, 2026, "  MERCADO  ",
				PageRequest.of(0, 1, Sort.by("description").ascending()));
		var secondPage = transactionService.list(user, 1, 2026, "mercado",
				PageRequest.of(1, 1, Sort.by("description").ascending()));
		var categoryMatch = transactionService.list(user, 1, 2026, "mora",
				PageRequest.of(0, 10, Sort.by("description").ascending()));

		assertThat(firstPage.getTotalElements()).isEqualTo(2);
		assertThat(firstPage.getTotalPages()).isEqualTo(2);
		assertThat(firstPage.getContent()).extracting(TransactionResponseDTO::description)
				.containsExactly("Mercado extra");
		assertThat(secondPage.getContent()).extracting(TransactionResponseDTO::description)
				.containsExactly("Mercado semanal");
		assertThat(categoryMatch.getContent()).extracting(TransactionResponseDTO::description)
				.containsExactly("Aluguel");
	}

	private User saveUser(String prefix) {
		User user = new User();
		user.setEmail(prefix + UUID.randomUUID() + "@mfc.local");
		user.setPassword("encoded-password");
		user.setRole(UserRole.USER);
		user.setEnabled(true);
		return userRepository.save(user);
	}

	private Category saveCategory(User user, String name) {
		Category category = new Category();
		category.setName(name);
		category.setType(CategoryType.EXPENSE);
		category.setUser(user);
		return categoryRepository.save(category);
	}

	private void saveTransaction(User user, Category category, String description, LocalDate date) {
		Transaction transaction = new Transaction();
		transaction.setDescription(description);
		transaction.setAmount(new BigDecimal("100.00"));
		transaction.setDate(date);
		transaction.setType(TransactionType.EXPENSE);
		transaction.setCategory(category);
		transaction.setUser(user);
		transactionRepository.save(transaction);
	}
}
