package br.com.devl.mfc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.dto.TransactionRequestDTO;
import br.com.devl.mfc.dto.TransactionResponseDTO;
import br.com.devl.mfc.entity.Category;
import br.com.devl.mfc.entity.Transaction;
import br.com.devl.mfc.entity.TransactionGroup;
import br.com.devl.mfc.enums.TransactionGroupType;
import br.com.devl.mfc.exception.BusinessException;
import br.com.devl.mfc.repository.CategoryRepository;
import br.com.devl.mfc.repository.TransactionGroupRepository;
import br.com.devl.mfc.repository.TransactionRepository;

@Service
public class TransactionService {
	private static final Pattern GROUP_SUFFIX = Pattern.compile("\\s*(\\(\\d+/\\d+\\)|\\[Recorrente])$");

	private final TransactionRepository transactionRepository;
	private final CategoryRepository categoryRepository;
	private final TransactionGroupRepository transactionGroupRepository;

	public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository,
			TransactionGroupRepository transactionGroupRepository) {
		this.transactionRepository = transactionRepository;
		this.categoryRepository = categoryRepository;
		this.transactionGroupRepository = transactionGroupRepository;
	}

	@Transactional
	public TransactionResponseDTO create(TransactionRequestDTO dto, User user) {
		Category category = categoryRepository.findByIdAndUser(dto.categoryId(), user)
					.orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Categoria não encontrada",
							HttpStatus.NOT_FOUND));

		validateTransaction(dto, category);

		int installments = dto.installments() == null ? 1 : dto.installments();
		int iterations = installments > 1 ? installments : dto.recurring() ? dto.occurrences() : 1;

		TransactionGroup group = null;
		if (iterations > 1) {
			group = new TransactionGroup();
			group.setUser(user);
			if (installments > 1) {
				group.setType(TransactionGroupType.INSTALLMENT);
				group.setTotalInstallments(installments);
			} else if (dto.recurring()) {
				group.setType(TransactionGroupType.RECURRING);
			}
			group = transactionGroupRepository.save(group);
		}

		BigDecimal valuePerEntry = installments > 1
					? dto.amount().divide(BigDecimal.valueOf(iterations), 2, RoundingMode.DOWN)
					: dto.amount();

		Transaction firstSaved = null;

		for (int i = 0; i < iterations; i++) {
			Transaction transaction = new Transaction();

			String suffix = "";
				if (installments > 1) {
				suffix = " (" + (i + 1) + "/" + iterations + ")";
			} else if (dto.recurring()) {
				suffix = " [Recorrente]";
			}

				transaction.setDescription(dto.description().trim() + suffix);
				BigDecimal entryAmount = installments > 1 && i == iterations - 1
						? dto.amount().subtract(valuePerEntry.multiply(BigDecimal.valueOf(iterations - 1)))
						: valuePerEntry;
				transaction.setAmount(entryAmount);
			transaction.setDate(dto.date().plusMonths(i));
			transaction.setType(dto.type());
			transaction.setCategory(category);
			transaction.setUser(user);
			transaction.setGroup(group);

			Transaction saved = transactionRepository.save(transaction);

			if (i == 0) {
				firstSaved = saved;
			}
		}

		return toResponseDTO(firstSaved);
	}

	@Transactional(readOnly = true)
	public Page<TransactionResponseDTO> list(User user, int month, int year, String search, Pageable pageable) {
		String normalizedSearch = search == null ? "" : search.trim();
		return transactionRepository.findByUserAndMonthAndYear(user, month, year, normalizedSearch, pageable)
				.map(this::toResponseDTO);
	}

	@Transactional(readOnly = true)
	public TransactionResponseDTO findById(Long id, User user) {
		Transaction transaction = transactionRepository.findByIdAndUser(id, user)
					.orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND", "Transação não encontrada",
							HttpStatus.NOT_FOUND));
		return toResponseDTO(transaction);
	}

	@Transactional
	public TransactionResponseDTO update(Long id, TransactionRequestDTO dto, User user) {
		Transaction transaction = transactionRepository.findByIdAndUser(id, user)
					.orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND", "Transação não encontrada",
							HttpStatus.NOT_FOUND));

		Category category = categoryRepository.findByIdAndUser(dto.categoryId(), user)
					.orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "Categoria não encontrada",
							HttpStatus.NOT_FOUND));

		validateTransaction(dto, category);

		transaction.setDescription(descriptionWithExistingGroupSuffix(dto.description(), transaction));
		transaction.setAmount(dto.amount());
		transaction.setDate(dto.date());
		transaction.setType(dto.type());
		transaction.setCategory(category);

		Transaction updated = transactionRepository.save(transaction);

		return toResponseDTO(updated);
	}

	@Transactional
	public void delete(Long id, User user) {
		Transaction transaction = transactionRepository.findByIdAndUser(id, user)
					.orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND", "Transação não encontrada",
							HttpStatus.NOT_FOUND));
		transactionRepository.delete(transaction);
	}

	@Transactional
	public void deleteGroupForward(Long id, User user) {
		Transaction transaction = transactionRepository.findByIdAndUser(id, user)
					.orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND", "Transação não encontrada",
							HttpStatus.NOT_FOUND));

		if (transaction.getGroup() == null) {
			throw new BusinessException("TRANSACTION_NOT_GROUPED", "Transação não pertence a um grupo",
					HttpStatus.BAD_REQUEST);
		}

		transactionRepository.deleteFromGroupOnwards(transaction.getGroup(), user, transaction.getDate(), Instant.now());
	}

	@Transactional
	public void deleteGroup(Long id, User user) {
		Transaction transaction = transactionRepository.findByIdAndUser(id, user)
				.orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND", "Transação não encontrada",
						HttpStatus.NOT_FOUND));
		if (transaction.getGroup() == null) {
			throw new BusinessException("TRANSACTION_NOT_GROUPED", "Transação não pertence a um grupo",
					HttpStatus.BAD_REQUEST);
		}
		transactionRepository.deleteByGroupAndUser(transaction.getGroup(), user, Instant.now());
	}

	private TransactionResponseDTO toResponseDTO(Transaction transaction) {
		String groupId = transaction.getGroup() != null ? transaction.getGroup().getId().toString() : null;
		TransactionGroupType groupType = transaction.getGroup() != null ? transaction.getGroup().getType() : null;
		return new TransactionResponseDTO(transaction.getId(), transaction.getDescription(), transaction.getAmount(),
					transaction.getDate(), transaction.getType(), transaction.getCategory().getName(),
					groupId, groupType);
	}

	private void validateTransaction(TransactionRequestDTO dto, Category category) {
		if (!category.getType().name().equals(dto.type().name())) {
			throw new BusinessException("O tipo da transação deve ser igual ao tipo da categoria!");
		}

		if (dto.amount() == null || dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new BusinessException("O valor deve ser maior que zero(0)!");
		}

		int installments = dto.installments() == null ? 1 : dto.installments();
		if (dto.recurring() && installments > 1) {
			throw new BusinessException("INVALID_GROUP_OPTIONS", "Uma transação não pode ser parcelada e recorrente ao mesmo tempo.",
					HttpStatus.BAD_REQUEST);
		}
		if (dto.recurring() && dto.occurrences() == null) {
			throw new BusinessException("O número de ocorrências é obrigatório para uma transação recorrente.");
		}
		if (!dto.recurring() && dto.occurrences() != null) {
			throw new BusinessException("Ocorrências só podem ser informadas em uma transação recorrente.");
		}
		if (installments > 1 && dto.type() != br.com.devl.mfc.enums.TransactionType.EXPENSE) {
			throw new BusinessException("Somente despesas podem ser parceladas.");
		}
	}

	private String descriptionWithExistingGroupSuffix(String description, Transaction transaction) {
		String normalized = GROUP_SUFFIX.matcher(description.trim()).replaceFirst("").trim();
		if (transaction.getGroup() == null) {
			return normalized;
		}
		Matcher matcher = GROUP_SUFFIX.matcher(transaction.getDescription());
		return matcher.find() ? normalized + " " + matcher.group(1) : normalized;
	}
}
