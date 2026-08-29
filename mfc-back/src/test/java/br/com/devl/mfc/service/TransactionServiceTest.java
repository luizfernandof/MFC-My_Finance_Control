package br.com.devl.mfc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.dto.TransactionRequestDTO;
import br.com.devl.mfc.entity.Category;
import br.com.devl.mfc.entity.Transaction;
import br.com.devl.mfc.entity.TransactionGroup;
import br.com.devl.mfc.enums.CategoryType;
import br.com.devl.mfc.enums.TransactionType;
import br.com.devl.mfc.exception.BusinessException;
import br.com.devl.mfc.repository.CategoryRepository;
import br.com.devl.mfc.repository.TransactionGroupRepository;
import br.com.devl.mfc.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@Mock
	private TransactionRepository transactionRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private TransactionGroupRepository transactionGroupRepository;

	private TransactionService service;
	private User user;
	private Category expenseCategory;

	@BeforeEach
	void setUp() {
		service = new TransactionService(transactionRepository, categoryRepository, transactionGroupRepository);
		user = new User();
		user.setId(7L);
		expenseCategory = new Category();
		expenseCategory.setId(9L);
		expenseCategory.setName("Moradia");
		expenseCategory.setType(CategoryType.EXPENSE);
		expenseCategory.setUser(user);
	}

	@Test
	void preservesTheExactTotalWhenAmountDoesNotDivideEvenly() {
		TransactionRequestDTO request = request(new BigDecimal("100.00"), 3, false, null);
		when(categoryRepository.findByIdAndUser(9L, user)).thenReturn(Optional.of(expenseCategory));
		when(transactionGroupRepository.save(any(TransactionGroup.class))).thenAnswer(invocation -> {
			TransactionGroup group = invocation.getArgument(0);
			group.setId(UUID.randomUUID());
			return group;
		});
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.create(request, user);

		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
		verify(transactionRepository, org.mockito.Mockito.times(3)).save(captor.capture());
		List<Transaction> entries = captor.getAllValues();
		assertThat(entries).extracting(Transaction::getAmount)
				.containsExactly(new BigDecimal("33.33"), new BigDecimal("33.33"), new BigDecimal("33.34"));
		assertThat(entries).extracting(Transaction::getDate)
				.containsExactly(LocalDate.of(2026, 8, 20), LocalDate.of(2026, 9, 20), LocalDate.of(2026, 10, 20));
		assertThat(entries).extracting(Transaction::getDescription)
				.containsExactly("Notebook (1/3)", "Notebook (2/3)", "Notebook (3/3)");
		assertThat(entries.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
				.isEqualByComparingTo("100.00");
	}

	@Test
	void rejectsRecurringAndInstallmentOptionsTogether() {
		when(categoryRepository.findByIdAndUser(9L, user)).thenReturn(Optional.of(expenseCategory));

		assertThatThrownBy(() -> service.create(request(new BigDecimal("100.00"), 3, true, 4), user))
				.isInstanceOf(BusinessException.class)
				.hasMessageContaining("parcelada e recorrente");

		verify(transactionGroupRepository, never()).save(any());
		verify(transactionRepository, never()).save(any());
	}

	private TransactionRequestDTO request(BigDecimal amount, Integer installments, boolean recurring,
			Integer occurrences) {
		return new TransactionRequestDTO(" Notebook ", amount, LocalDate.of(2026, 8, 20), 9L,
				TransactionType.EXPENSE, installments, recurring, occurrences);
	}
}
