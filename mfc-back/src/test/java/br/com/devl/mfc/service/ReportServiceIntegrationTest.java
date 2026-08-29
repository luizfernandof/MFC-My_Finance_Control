package br.com.devl.mfc.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.auth.enums.UserRole;
import br.com.devl.mfc.auth.repository.UserRepository;
import br.com.devl.mfc.entity.Category;
import br.com.devl.mfc.entity.Transaction;
import br.com.devl.mfc.enums.CategoryType;
import br.com.devl.mfc.enums.TransactionType;
import br.com.devl.mfc.repository.CategoryRepository;
import br.com.devl.mfc.repository.TransactionRepository;

@SpringBootTest
@ActiveProfiles("test")
class ReportServiceIntegrationTest {

	@Autowired
	private ReportService reportService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Test
	void generatesReportWithLazyCategoriesWhenOpenInViewIsDisabled() throws Exception {
		User user = new User();
		user.setEmail("report-" + UUID.randomUUID() + "@mfc.local");
		user.setPassword("encoded-password");
		user.setRole(UserRole.USER);
		user.setEnabled(true);
		user = userRepository.save(user);

		String categoryName = "Moradia Teste";
		Category category = new Category();
		category.setName(categoryName);
		category.setType(CategoryType.EXPENSE);
		category.setUser(user);
		category = categoryRepository.save(category);

		Transaction transaction = new Transaction();
		transaction.setDescription("Aluguel residencial");
		transaction.setAmount(new BigDecimal("1650.00"));
		transaction.setDate(LocalDate.of(2026, 7, 8));
		transaction.setType(TransactionType.EXPENSE);
		transaction.setCategory(category);
		transaction.setUser(user);
		transactionRepository.save(transaction);

		byte[] bytes = reportService.generateMonthlyTransactionsPdf(user, 7, 2026);
		byte[] categoryBytes = reportService.generateMonthlyCategoryAnalysisPdf(user, 7, 2026);

		assertThat(bytes).startsWith(new byte[] { '%', 'P', 'D', 'F' });
		try (PDDocument document = Loader.loadPDF(bytes)) {
			assertThat(new PDFTextStripper().getText(document))
					.contains("MY FINANCE CONTROL", "EXTRATO MENSAL", "Relatório de Transações",
							"Aluguel residencial", categoryName);
		}
		assertThat(categoryBytes).startsWith(new byte[] { '%', 'P', 'D', 'F' });
		try (PDDocument document = Loader.loadPDF(categoryBytes)) {
			assertThat(new PDFTextStripper().getText(document))
					.contains("MY FINANCE CONTROL", "GASTOS POR CATEGORIA", "Aluguel residencial", categoryName);
		}
	}
}
