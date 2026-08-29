package br.com.devl.mfc.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.entity.Category;
import br.com.devl.mfc.entity.Transaction;
import br.com.devl.mfc.enums.TransactionType;
import br.com.devl.mfc.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

	@Mock
	private TransactionRepository repository;

	@Test
	void generatesAReadablePdfForAnEmptyPeriod() throws Exception {
		User user = new User();
		when(repository.findByUserAndMonthAndYearOrderByDate(user, 8, 2026)).thenReturn(List.of());
		byte[] bytes = new ReportService(repository).generateMonthlyTransactionsPdf(user, 8, 2026);

		assertThat(bytes).startsWith(new byte[] { '%', 'P', 'D', 'F' });
		try (PDDocument document = Loader.loadPDF(bytes)) {
			assertThat(document.getNumberOfPages()).isEqualTo(1);
			assertThat(new PDFTextStripper().getText(document))
					.contains("MY FINANCE CONTROL", "EXTRATO MENSAL", "Relatório de Transações",
							"ENTRADAS", "SAÍDAS", "SALDO", "Nenhuma transação encontrada");
			assertThat(document.getDocumentInformation().getAuthor()).isEqualTo("My Finance Control");
		}
	}

	@Test
	void replacesUnsupportedGlyphsInsteadOfFailingTheWholeReport() throws Exception {
		User user = new User();
		Category category = new Category();
		category.setName("Alimentação");
		Transaction transaction = new Transaction();
		transaction.setDescription("Almoço 🥗");
		transaction.setAmount(new BigDecimal("42.50"));
		transaction.setDate(LocalDate.of(2026, 8, 20));
		transaction.setType(TransactionType.EXPENSE);
		transaction.setCategory(category);
		when(repository.findByUserAndMonthAndYearOrderByDate(user, 8, 2026)).thenReturn(List.of(transaction));

		byte[] bytes = new ReportService(repository).generateMonthlyTransactionsPdf(user, 8, 2026);

		try (PDDocument document = Loader.loadPDF(bytes)) {
			assertThat(new PDFTextStripper().getText(document))
					.contains("Almoço ?", "Alimentação", "TRANSAÇÕES DO MÊS", "1 lançamento");
		}
	}

	@Test
	void repeatsTheVisualHeaderAndTableOnContinuationPages() throws Exception {
		User user = new User();
		user.setEmail("teste@mfc.local");
		Category category = new Category();
		category.setName("Moradia");
		Transaction transaction = new Transaction();
		transaction.setDescription("Aluguel residencial");
		transaction.setAmount(new BigDecimal("1650.00"));
		transaction.setDate(LocalDate.of(2026, 8, 8));
		transaction.setType(TransactionType.EXPENSE);
		transaction.setCategory(category);
		when(repository.findByUserAndMonthAndYearOrderByDate(user, 8, 2026))
				.thenReturn(Collections.nCopies(30, transaction));

		byte[] bytes = new ReportService(repository).generateMonthlyTransactionsPdf(user, 8, 2026);

		try (PDDocument document = Loader.loadPDF(bytes)) {
			String text = new PDFTextStripper().getText(document);
			assertThat(document.getNumberOfPages()).isGreaterThan(1);
			assertThat(text).contains("Agosto de 2026 - continuação", "Página 2 de 2");
		}
	}

	@Test
	void generatesCategoryAnalysisWithSharesComparisonAndLargestExpense() throws Exception {
		User user = new User();
		user.setEmail("teste@mfc.local");
		Category housing = category("Moradia");
		Category food = category("Alimentação");
		List<Transaction> current = List.of(
				transaction("Aluguel residencial", "1650.00", LocalDate.of(2026, 7, 8), housing),
				transaction("Supermercado", "800.00", LocalDate.of(2026, 7, 11), food),
				transaction("Padaria", "20.00", LocalDate.of(2026, 7, 15), food));
		List<Transaction> previous = List.of(
				transaction("Aluguel residencial", "1500.00", LocalDate.of(2026, 6, 8), housing),
				transaction("Supermercado", "1000.00", LocalDate.of(2026, 6, 11), food));
		when(repository.findByUserAndMonthAndYearOrderByDate(user, 7, 2026)).thenReturn(current);
		when(repository.findByUserAndMonthAndYearOrderByDate(user, 6, 2026)).thenReturn(previous);

		byte[] bytes = new ReportService(repository).generateMonthlyCategoryAnalysisPdf(user, 7, 2026);

		try (PDDocument document = Loader.loadPDF(bytes)) {
			String text = new PDFTextStripper().getText(document);
			assertThat(document.getDocumentInformation().getTitle()).isEqualTo("Gastos por categoria - Julho de 2026");
			assertThat(text).contains("MY FINANCE CONTROL", "GASTOS POR CATEGORIA", "TOTAL GASTO",
					"RANKING COMPLETO", "Comparado com Junho de 2026", "Moradia", "Alimentação",
					"Maior: R$ 1.650,00", "Maior: R$ 800,00");
		}
	}

	@Test
	void paginatesTheCompleteCategoryRanking() throws Exception {
		User user = new User();
		user.setEmail("teste@mfc.local");
		List<Transaction> transactions = new ArrayList<>();
		for (int index = 1; index <= 18; index++) {
			transactions.add(transaction("Despesa " + index, String.valueOf(100 + index),
					LocalDate.of(2026, 7, Math.min(index, 28)), category("Categoria " + index)));
		}
		when(repository.findByUserAndMonthAndYearOrderByDate(user, 7, 2026)).thenReturn(transactions);
		when(repository.findByUserAndMonthAndYearOrderByDate(user, 6, 2026)).thenReturn(List.of());

		byte[] bytes = new ReportService(repository).generateMonthlyCategoryAnalysisPdf(user, 7, 2026);

		try (PDDocument document = Loader.loadPDF(bytes)) {
			String text = new PDFTextStripper().getText(document);
			assertThat(document.getNumberOfPages()).isGreaterThan(1);
			assertThat(text).contains("Julho de 2026 - continuação", "Categoria 18", "Página 2 de 2");
		}
	}

	@Test
	void comparesJanuaryWithDecemberFromThePreviousYear() throws Exception {
		User user = new User();
		user.setEmail("teste@mfc.local");
		when(repository.findByUserAndMonthAndYearOrderByDate(user, 1, 2026)).thenReturn(List.of());
		when(repository.findByUserAndMonthAndYearOrderByDate(user, 12, 2025)).thenReturn(List.of());

		byte[] bytes = new ReportService(repository).generateMonthlyCategoryAnalysisPdf(user, 1, 2026);

		try (PDDocument document = Loader.loadPDF(bytes)) {
			assertThat(new PDFTextStripper().getText(document))
					.contains("Janeiro de 2026", "Comparado com Dezembro de 2025", "Nenhuma despesa encontrada");
		}
	}

	private static Category category(String name) {
		Category category = new Category();
		category.setName(name);
		return category;
	}

	private static Transaction transaction(String description, String amount, LocalDate date, Category category) {
		Transaction transaction = new Transaction();
		transaction.setDescription(description);
		transaction.setAmount(new BigDecimal(amount));
		transaction.setDate(date);
		transaction.setType(TransactionType.EXPENSE);
		transaction.setCategory(category);
		return transaction;
	}
}
