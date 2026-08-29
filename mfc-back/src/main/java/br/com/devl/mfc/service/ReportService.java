package br.com.devl.mfc.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.entity.Transaction;
import br.com.devl.mfc.enums.TransactionType;
import br.com.devl.mfc.exception.BusinessException;
import br.com.devl.mfc.repository.TransactionRepository;

@Service
public class ReportService {

	private static final Locale BRAZIL = Locale.forLanguageTag("pt-BR");
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final PDFont REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
	private static final PDFont BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
	private static final PDFont BOLD_ITALIC = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);
	private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
	private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
	private static final float MARGIN = 32;
	private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
	private static final float ROW_HEIGHT = 27;
	private static final float[] COLUMN_WIDTHS = { 62, 196, 105, 70, 98 };
	private static final float CATEGORY_ROW_HEIGHT = 42;
	private static final float[] CATEGORY_COLUMN_WIDTHS = { 35, 180, 58, 86, 86, 86 };

	private static final Color BLUE = new Color(37, 99, 235);
	private static final Color BLUE_DARK = new Color(30, 64, 175);
	private static final Color BLUE_LIGHT = new Color(239, 246, 255);
	private static final Color SLATE_50 = new Color(248, 250, 252);
	private static final Color SLATE_100 = new Color(241, 245, 249);
	private static final Color SLATE_200 = new Color(226, 232, 240);
	private static final Color SLATE_400 = new Color(148, 163, 184);
	private static final Color SLATE_500 = new Color(100, 116, 139);
	private static final Color SLATE_700 = new Color(51, 65, 85);
	private static final Color SLATE_800 = new Color(30, 41, 59);
	private static final Color WHITE = new Color(255, 255, 255);
	private static final Color EMERALD = new Color(16, 185, 129);
	private static final Color EMERALD_LIGHT = new Color(236, 253, 245);
	private static final Color ROSE = new Color(244, 63, 94);
	private static final Color ROSE_LIGHT = new Color(255, 241, 242);
	private static final Color AMBER = new Color(245, 158, 11);

	private final TransactionRepository transactionRepository;

	public ReportService(TransactionRepository transactionRepository) {
		this.transactionRepository = transactionRepository;
	}

	@Transactional(readOnly = true)
	public byte[] generateMonthlyTransactionsPdf(User user, int month, int year) {
		List<Transaction> transactions = transactionRepository.findByUserAndMonthAndYearOrderByDate(user, month, year);
		String period = capitalize(LocalDate.of(year, month, 1).getMonth().getDisplayName(TextStyle.FULL, BRAZIL))
				+ " de " + year;
		Totals totals = calculateTotals(transactions);

		try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			addMetadata(document, "Extrato mensal - " + period,
					"Relatório mensal de receitas, despesas e saldo");
			PageContext page = newFirstPage(document, user, period, totals, transactions);

			if (transactions.isEmpty()) {
				drawEmptyState(page);
				page.close();
			} else {
				for (Transaction transaction : transactions) {
					if (page.y() < 72) {
						page.close();
						page = newContinuationPage(document, user, period);
					}
					page = drawTransaction(page, transaction);
				}
				page.close();
			}

			addFooters(document);
			document.save(output);
			return output.toByteArray();
		} catch (IOException ex) {
			throw new BusinessException("PDF_GENERATION_ERROR", "Não foi possível gerar o relatório.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Transactional(readOnly = true)
	public byte[] generateMonthlyCategoryAnalysisPdf(User user, int month, int year) {
		LocalDate selectedMonth = LocalDate.of(year, month, 1);
		LocalDate previousMonth = selectedMonth.minusMonths(1);
		List<Transaction> currentTransactions = transactionRepository
				.findByUserAndMonthAndYearOrderByDate(user, selectedMonth.getMonthValue(), selectedMonth.getYear());
		List<Transaction> previousTransactions = transactionRepository
				.findByUserAndMonthAndYearOrderByDate(user, previousMonth.getMonthValue(), previousMonth.getYear());
		String period = periodLabel(selectedMonth);
		String previousPeriod = periodLabel(previousMonth);
		List<CategoryAnalysis> categories = calculateCategoryAnalysis(currentTransactions, previousTransactions);
		BigDecimal currentTotal = totalExpenses(currentTransactions);
		BigDecimal previousTotal = totalExpenses(previousTransactions);

		try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			addMetadata(document, "Gastos por categoria - " + period,
					"Participação, comparação mensal e maiores despesas por categoria");
			PageContext page = newCategoryFirstPage(document, user, period, previousPeriod, currentTotal,
					previousTotal, categories);

			if (categories.isEmpty()) {
				drawCategoryEmptyState(page);
				page.close();
			} else {
				for (int index = 0; index < categories.size(); index++) {
					if (page.y() < 82) {
						page.close();
						page = newCategoryContinuationPage(document, user, period, previousPeriod);
					}
					page = drawCategoryRow(page, categories.get(index), index);
				}
				page.close();
			}

			addFooters(document);
			document.save(output);
			return output.toByteArray();
		} catch (IOException ex) {
			throw new BusinessException("PDF_GENERATION_ERROR", "Não foi possível gerar o relatório.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private PageContext newFirstPage(PDDocument document, User user, String period, Totals totals,
			List<Transaction> transactions) throws IOException {
		PageContext page = newPage(document);
		drawBrandHeader(page.content(), user, period, "EXTRATO MENSAL", "Relatório de Transações", false);
		drawSectionLabel(page.content(), "RESUMO DO PERÍODO", 696);
		drawSummaryCards(page.content(), totals);
		drawTopCategories(page.content(), transactions);
		drawTransactionsTitle(page.content(), transactions.size(), 500);
		return drawTableHeader(new PageContext(page.content(), 472));
	}

	private PageContext newContinuationPage(PDDocument document, User user, String period) throws IOException {
		PageContext page = newPage(document);
		drawBrandHeader(page.content(), user, period, "EXTRATO MENSAL", "Relatório de Transações", true);
		drawTransactionsTitle(page.content(), -1, 728);
		return drawTableHeader(new PageContext(page.content(), 700));
	}

	private PageContext newCategoryFirstPage(PDDocument document, User user, String period, String previousPeriod,
			BigDecimal currentTotal, BigDecimal previousTotal, List<CategoryAnalysis> categories) throws IOException {
		PageContext page = newPage(document);
		drawBrandHeader(page.content(), user, period, "GASTOS POR CATEGORIA", "Análise mensal de despesas", false);
		drawSectionLabel(page.content(), "VISÃO GERAL", 696);
		drawCategorySummaryCards(page.content(), currentTotal, previousTotal, categories.size());
		drawCategoryRankingTitle(page.content(), categories.size(), previousPeriod, 601);
		return drawCategoryTableHeader(new PageContext(page.content(), 573));
	}

	private PageContext newCategoryContinuationPage(PDDocument document, User user, String period,
			String previousPeriod) throws IOException {
		PageContext page = newPage(document);
		drawBrandHeader(page.content(), user, period, "GASTOS POR CATEGORIA", "Análise mensal de despesas", true);
		drawCategoryRankingTitle(page.content(), -1, previousPeriod, 728);
		return drawCategoryTableHeader(new PageContext(page.content(), 700));
	}

	private void drawCategorySummaryCards(PDPageContentStream content, BigDecimal currentTotal,
			BigDecimal previousTotal, int categoryCount) throws IOException {
		float gap = 10;
		float cardWidth = (CONTENT_WIDTH - (gap * 2)) / 3;
		drawSummaryCard(content, MARGIN, 620, cardWidth, "TOTAL GASTO", currency(currentTotal), ROSE, ROSE_LIGHT);
		drawSummaryCard(content, MARGIN + cardWidth + gap, 620, cardWidth, "CATEGORIAS",
				categoryCount + (categoryCount == 1 ? " ativa" : " ativas"), BLUE, BLUE_LIGHT);

		Color variationColor = expenseVariationColor(currentTotal, previousTotal);
		Color variationBackground = variationColor == EMERALD ? EMERALD_LIGHT
				: variationColor == ROSE ? ROSE_LIGHT : BLUE_LIGHT;
		drawSummaryCard(content, MARGIN + (cardWidth + gap) * 2, 620, cardWidth, "VS. MÊS ANTERIOR",
				expenseVariationLabel(currentTotal, previousTotal), variationColor, variationBackground);
	}

	private void drawCategoryRankingTitle(PDPageContentStream content, int categoryCount, String previousPeriod,
			float y) throws IOException {
		writeText(content, "RANKING COMPLETO", BOLD, 7, MARGIN, y, SLATE_400);
		String comparison = "Comparado com " + previousPeriod;
		if (categoryCount >= 0) {
			comparison = categoryCount + (categoryCount == 1 ? " categoria - " : " categorias - ") + comparison;
		}
		writeRightText(content, comparison, REGULAR, 7.5f, PAGE_WIDTH - MARGIN, y, SLATE_500);
	}

	private PageContext drawCategoryTableHeader(PageContext page) throws IOException {
		String[] headers = { "#", "CATEGORIA / MAIOR LANÇAMENTO", "PART.", "ATUAL", "ANTERIOR", "VARIAÇÃO" };
		float x = MARGIN;
		drawRoundedRect(page.content(), MARGIN, page.y() - 21, CONTENT_WIDTH, 25, 7, BLUE_LIGHT, BLUE_LIGHT);
		for (int index = 0; index < headers.length; index++) {
			if (index >= 2) {
				writeRightText(page.content(), headers[index], BOLD, 6.5f,
						x + CATEGORY_COLUMN_WIDTHS[index] - 9, page.y() - 9, BLUE_DARK);
			} else {
				writeText(page.content(), headers[index], BOLD, 6.5f, x + 9, page.y() - 9, BLUE_DARK);
			}
			x += CATEGORY_COLUMN_WIDTHS[index];
		}
		return new PageContext(page.content(), page.y() - 29);
	}

	private PageContext drawCategoryRow(PageContext page, CategoryAnalysis category, int index) throws IOException {
		float y = page.y();
		Color rowColor = index % 2 == 0 ? WHITE : SLATE_50;
		drawRoundedRect(page.content(), MARGIN, y - 34, CONTENT_WIDTH, 38, 6, rowColor, SLATE_100);

		float x = MARGIN;
		Color rankColor = switch (index) {
			case 0 -> BLUE;
			case 1 -> EMERALD;
			case 2 -> AMBER;
			default -> SLATE_400;
		};
		drawRoundedRect(page.content(), x + 7, y - 23, 23, 20, 6,
				index < 3 ? new Color(248, 250, 252) : SLATE_100,
				index < 3 ? new Color(248, 250, 252) : SLATE_100);
		writeCenteredText(page.content(), "#" + (index + 1), BOLD, 7, x + 7, y - 16, 23, rankColor);
		x += CATEGORY_COLUMN_WIDTHS[0];

		writeText(page.content(), truncate(category.name(), BOLD, 8.5f, CATEGORY_COLUMN_WIDTHS[1] - 16), BOLD,
				8.5f, x + 8, y - 11, SLATE_700);
		String largest = "Maior: " + currency(category.largestAmount()) + " - " + category.largestDescription();
		writeText(page.content(), truncate(largest, REGULAR, 6.8f, CATEGORY_COLUMN_WIDTHS[1] - 16), REGULAR,
				6.8f, x + 8, y - 26, SLATE_400);
		x += CATEGORY_COLUMN_WIDTHS[1];

		writeRightText(page.content(), percent(category.sharePercent()), BOLD, 7.5f,
				x + CATEGORY_COLUMN_WIDTHS[2] - 9, y - 17, BLUE);
		x += CATEGORY_COLUMN_WIDTHS[2];
		writeRightText(page.content(), currency(category.currentTotal()), BOLD, 7.5f,
				x + CATEGORY_COLUMN_WIDTHS[3] - 9, y - 17, SLATE_700);
		x += CATEGORY_COLUMN_WIDTHS[3];
		writeRightText(page.content(), currency(category.previousTotal()), REGULAR, 7.5f,
				x + CATEGORY_COLUMN_WIDTHS[4] - 9, y - 17, SLATE_500);
		x += CATEGORY_COLUMN_WIDTHS[4];

		Color variationColor = expenseVariationColor(category.currentTotal(), category.previousTotal());
		Color variationBackground = variationColor == EMERALD ? EMERALD_LIGHT
				: variationColor == ROSE ? ROSE_LIGHT : BLUE_LIGHT;
		drawRoundedRect(page.content(), x + 9, y - 24, CATEGORY_COLUMN_WIDTHS[5] - 18, 20, 6,
				variationBackground, variationBackground);
		writeCenteredText(page.content(), expenseVariationLabel(category.currentTotal(), category.previousTotal()),
				BOLD, 6.5f, x + 9, y - 17, CATEGORY_COLUMN_WIDTHS[5] - 18, variationColor);

		return new PageContext(page.content(), y - CATEGORY_ROW_HEIGHT);
	}

	private void drawCategoryEmptyState(PageContext page) throws IOException {
		drawRoundedRect(page.content(), MARGIN, 410, CONTENT_WIDTH, 118, 12, WHITE, SLATE_200);
		drawRoundedRect(page.content(), MARGIN + 20, 451, 36, 36, 10, BLUE_LIGHT, BLUE_LIGHT);
		writeCenteredText(page.content(), "-", BOLD, 18, MARGIN + 20, 461, 36, BLUE);
		writeText(page.content(), "Nenhuma despesa encontrada", BOLD, 12, MARGIN + 72, 471, SLATE_800);
		writeText(page.content(), "Não há categorias de saída para analisar neste período.", REGULAR, 9,
				MARGIN + 72, 451, SLATE_500);
	}

	private PageContext newPage(PDDocument document) throws IOException {
		PDPage page = new PDPage(PDRectangle.A4);
		document.addPage(page);
		PDPageContentStream content = new PDPageContentStream(document, page);
		fillRect(content, 0, 0, PAGE_WIDTH, PAGE_HEIGHT, SLATE_50);
		return new PageContext(content, 0);
	}

	private void drawBrandHeader(PDPageContentStream content, User user, String period, String title, String subtitle,
			boolean compact) throws IOException {
		float height = compact ? 86 : 126;
		float bottom = PAGE_HEIGHT - height;
		fillRect(content, 0, bottom, PAGE_WIDTH, height, BLUE);
		fillRect(content, 0, bottom, 7, height, BLUE_DARK);

		drawRoundedRect(content, MARGIN, PAGE_HEIGHT - 59, 32, 32, 9, WHITE, WHITE);
		writeCenteredText(content, "MFC", BOLD, 9, MARGIN, PAGE_HEIGHT - 48, 32, BLUE);
		writeText(content, "MY FINANCE CONTROL", BOLD, 13, 74, PAGE_HEIGHT - 38, WHITE);
		writeText(content, "CONTROLE FINANCEIRO PESSOAL", REGULAR, 6.5f, 74, PAGE_HEIGHT - 52,
				new Color(219, 234, 254));

		String account = truncate(user.getEmail(), REGULAR, 8, 190);
		writeRightText(content, account, REGULAR, 8, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 42,
				new Color(219, 234, 254));

		if (compact) {
			writeText(content, title, BOLD_ITALIC, 15, MARGIN, bottom + 17, WHITE);
			writeRightText(content, period + " - continuação", BOLD, 9, PAGE_WIDTH - MARGIN, bottom + 18,
					new Color(219, 234, 254));
			return;
		}

		writeText(content, title, BOLD_ITALIC, 22, MARGIN, bottom + 28, WHITE);
		writeText(content, subtitle, REGULAR, 9, MARGIN, bottom + 14, new Color(219, 234, 254));
		writeRightText(content, period, BOLD, 11, PAGE_WIDTH - MARGIN, bottom + 20, WHITE);
	}

	private void drawSectionLabel(PDPageContentStream content, String label, float y) throws IOException {
		writeText(content, label, BOLD, 7, MARGIN, y, SLATE_400);
	}

	private void drawSummaryCards(PDPageContentStream content, Totals totals) throws IOException {
		float gap = 10;
		float cardWidth = (CONTENT_WIDTH - (gap * 2)) / 3;
		float y = 620;
		drawSummaryCard(content, MARGIN, y, cardWidth, "ENTRADAS", currency(totals.income()), EMERALD,
				EMERALD_LIGHT);
		drawSummaryCard(content, MARGIN + cardWidth + gap, y, cardWidth, "SAÍDAS", currency(totals.expense()),
				ROSE, ROSE_LIGHT);
		Color balanceColor = totals.balance().signum() >= 0 ? BLUE : ROSE;
		Color balanceBackground = totals.balance().signum() >= 0 ? BLUE_LIGHT : ROSE_LIGHT;
		drawSummaryCard(content, MARGIN + (cardWidth + gap) * 2, y, cardWidth, "SALDO",
				currency(totals.balance()), balanceColor, balanceBackground);
	}

	private void drawSummaryCard(PDPageContentStream content, float x, float y, float width, String label,
			String value, Color accent, Color iconBackground) throws IOException {
		drawRoundedRect(content, x, y, width, 62, 10, WHITE, SLATE_200);
		drawRoundedRect(content, x + 13, y + 34, 16, 16, 5, iconBackground, iconBackground);
		fillCircle(content, x + 21, y + 42, 3, accent);
		writeText(content, label, BOLD, 7, x + 36, y + 40, SLATE_400);
		writeText(content, truncate(value, BOLD, 15, width - 26), BOLD, 15, x + 13, y + 17, accent);
	}

	private void drawTopCategories(PDPageContentStream content, List<Transaction> transactions) throws IOException {
		drawSectionLabel(content, "MAIORES DESPESAS POR CATEGORIA", 601);
		List<CategoryTotal> categories = calculateTopCategories(transactions);
		if (categories.isEmpty()) {
			drawRoundedRect(content, MARGIN, 524, CONTENT_WIDTH, 57, 10, WHITE, SLATE_200);
			writeText(content, "Nenhuma saída registrada neste período.", REGULAR, 9, MARGIN + 16, 550,
					SLATE_500);
			return;
		}

		float gap = 10;
		float cardWidth = (CONTENT_WIDTH - (gap * 2)) / 3;
		Color[] colors = { BLUE, EMERALD, AMBER };
		for (int index = 0; index < 3; index++) {
			float x = MARGIN + (cardWidth + gap) * index;
			drawRoundedRect(content, x, 524, cardWidth, 57, 10, WHITE, SLATE_200);
			if (index >= categories.size()) {
				writeCenteredText(content, "-", BOLD, 12, x, 550, cardWidth, SLATE_200);
				continue;
			}
			CategoryTotal category = categories.get(index);
			fillCircle(content, x + 16, 553, 4, colors[index]);
			writeText(content, truncate(category.name(), BOLD, 8.5f, cardWidth - 55), BOLD, 8.5f, x + 27,
					553, SLATE_700);
			drawRoundedRect(content, x + cardWidth - 31, 544, 20, 18, 5,
					new Color(248, 250, 252), new Color(248, 250, 252));
			writeCenteredText(content, "#" + (index + 1), BOLD, 7, x + cardWidth - 31, 550, 20, colors[index]);
			writeText(content, currency(category.total()), REGULAR, 8, x + 27, 537, SLATE_400);
		}
	}

	private void drawTransactionsTitle(PDPageContentStream content, int count, float y) throws IOException {
		writeText(content, "TRANSAÇÕES DO MÊS", BOLD, 7, MARGIN, y, SLATE_400);
		if (count >= 0) {
			String label = count + (count == 1 ? " lançamento" : " lançamentos");
			writeRightText(content, label, REGULAR, 8, PAGE_WIDTH - MARGIN, y, SLATE_500);
		}
	}

	private PageContext drawTableHeader(PageContext page) throws IOException {
		String[] headers = { "DATA", "DESCRIÇÃO", "CATEGORIA", "TIPO", "VALOR" };
		float x = MARGIN;
		drawRoundedRect(page.content(), MARGIN, page.y() - 21, CONTENT_WIDTH, 25, 7, BLUE_LIGHT, BLUE_LIGHT);
		for (int index = 0; index < headers.length; index++) {
			if (index == headers.length - 1) {
				writeRightText(page.content(), headers[index], BOLD, 7, x + COLUMN_WIDTHS[index] - 10,
						page.y() - 9, BLUE_DARK);
			} else {
				writeText(page.content(), headers[index], BOLD, 7, x + 10, page.y() - 9, BLUE_DARK);
			}
			x += COLUMN_WIDTHS[index];
		}
		return new PageContext(page.content(), page.y() - 27);
	}

	private PageContext drawTransaction(PageContext page, Transaction transaction) throws IOException {
		float y = page.y();
		Color rowColor = ((int) ((472 - y) / ROW_HEIGHT)) % 2 == 0 ? WHITE : SLATE_50;
		drawRoundedRect(page.content(), MARGIN, y - 21, CONTENT_WIDTH, 25, 5, rowColor, SLATE_100);

		float x = MARGIN;
		writeText(page.content(), transaction.getDate().format(DATE_FORMAT), REGULAR, 8, x + 10, y - 9, SLATE_500);
		x += COLUMN_WIDTHS[0];
		writeText(page.content(), truncate(transaction.getDescription(), BOLD, 8, COLUMN_WIDTHS[1] - 18), BOLD, 8,
				x + 9, y - 9, SLATE_700);
		x += COLUMN_WIDTHS[1];
		writeText(page.content(), truncate(transaction.getCategory().getName(), REGULAR, 8, COLUMN_WIDTHS[2] - 18),
				REGULAR, 8, x + 9, y - 9, SLATE_500);
		x += COLUMN_WIDTHS[2];

		boolean income = transaction.getType() == TransactionType.INCOME;
		Color typeColor = income ? EMERALD : ROSE;
		Color typeBackground = income ? EMERALD_LIGHT : ROSE_LIGHT;
		drawRoundedRect(page.content(), x + 8, y - 16, COLUMN_WIDTHS[3] - 16, 15, 5, typeBackground, typeBackground);
		writeCenteredText(page.content(), income ? "ENTRADA" : "SAÍDA", BOLD, 6, x + 8, y - 11,
				COLUMN_WIDTHS[3] - 16, typeColor);
		x += COLUMN_WIDTHS[3];

		String value = (income ? "+ " : "- ") + currency(transaction.getAmount());
		writeRightText(page.content(), truncate(value, BOLD, 8, COLUMN_WIDTHS[4] - 16), BOLD, 8,
				x + COLUMN_WIDTHS[4] - 9, y - 9, typeColor);
		return new PageContext(page.content(), y - ROW_HEIGHT);
	}

	private void drawEmptyState(PageContext page) throws IOException {
		drawRoundedRect(page.content(), MARGIN, 361, CONTENT_WIDTH, 108, 12, WHITE, SLATE_200);
		drawRoundedRect(page.content(), MARGIN + 20, 397, 36, 36, 10, BLUE_LIGHT, BLUE_LIGHT);
		writeCenteredText(page.content(), "-", BOLD, 18, MARGIN + 20, 407, 36, BLUE);
		writeText(page.content(), "Nenhuma transação encontrada", BOLD, 12, MARGIN + 72, 416, SLATE_800);
		writeText(page.content(), "Cadastre lançamentos para acompanhar seu período com mais clareza.", REGULAR, 9,
				MARGIN + 72, 397, SLATE_500);
	}

	private Totals calculateTotals(List<Transaction> transactions) {
		BigDecimal income = transactions.stream().filter(t -> t.getType() == TransactionType.INCOME)
				.map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal expense = transactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
				.map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new Totals(income, expense, income.subtract(expense));
	}

	private List<CategoryAnalysis> calculateCategoryAnalysis(List<Transaction> currentTransactions,
			List<Transaction> previousTransactions) {
		Map<String, BigDecimal> currentTotals = new HashMap<>();
		Map<String, BigDecimal> previousTotals = new HashMap<>();
		Map<String, Transaction> largestTransactions = new HashMap<>();

		currentTransactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE).forEach(transaction -> {
			String category = transaction.getCategory().getName();
			currentTotals.merge(category, transaction.getAmount(), BigDecimal::add);
			largestTransactions.merge(category, transaction,
					(current, candidate) -> candidate.getAmount().compareTo(current.getAmount()) > 0 ? candidate : current);
		});
		previousTransactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
				.forEach(transaction -> previousTotals.merge(transaction.getCategory().getName(), transaction.getAmount(),
						BigDecimal::add));

		BigDecimal total = currentTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		return currentTotals.entrySet().stream().map(entry -> {
			Transaction largest = largestTransactions.get(entry.getKey());
			return new CategoryAnalysis(entry.getKey(), entry.getValue(),
					previousTotals.getOrDefault(entry.getKey(), BigDecimal.ZERO), percentage(entry.getValue(), total),
					largest.getDescription(), largest.getAmount());
		}).sorted((left, right) -> right.currentTotal().compareTo(left.currentTotal())).toList();
	}

	private BigDecimal totalExpenses(List<Transaction> transactions) {
		return transactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
				.map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private double percentage(BigDecimal value, BigDecimal total) {
		if (total.signum() == 0) return 0;
		return value.divide(total, 6, java.math.RoundingMode.HALF_UP).doubleValue() * 100;
	}

	private List<CategoryTotal> calculateTopCategories(List<Transaction> transactions) {
		Map<String, BigDecimal> totals = new HashMap<>();
		transactions.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
				.forEach(t -> totals.merge(t.getCategory().getName(), t.getAmount(), BigDecimal::add));
		return totals.entrySet().stream().map(entry -> new CategoryTotal(entry.getKey(), entry.getValue()))
				.sorted((left, right) -> right.total().compareTo(left.total())).limit(3).toList();
	}

	private void addMetadata(PDDocument document, String title, String subject) {
		PDDocumentInformation information = new PDDocumentInformation();
		information.setTitle(title);
		information.setAuthor("My Finance Control");
		information.setSubject(subject);
		document.setDocumentInformation(information);
	}

	private void addFooters(PDDocument document) throws IOException {
		String generatedAt = "Gerado em " + LocalDate.now().format(DATE_FORMAT);
		int totalPages = document.getNumberOfPages();
		for (int index = 0; index < totalPages; index++) {
			PDPage page = document.getPage(index);
			try (PDPageContentStream content = new PDPageContentStream(document, page,
					PDPageContentStream.AppendMode.APPEND, true, true)) {
				fillRect(content, MARGIN, 38, CONTENT_WIDTH, 1, SLATE_200);
				writeText(content, "MY FINANCE CONTROL", BOLD, 6.5f, MARGIN, 23, SLATE_400);
				writeCenteredText(content, generatedAt, REGULAR, 7, 0, 23, PAGE_WIDTH, SLATE_400);
				writeRightText(content, "Página " + (index + 1) + " de " + totalPages, REGULAR, 7,
						PAGE_WIDTH - MARGIN, 23, SLATE_400);
			}
		}
	}

	private void drawRoundedRect(PDPageContentStream content, float x, float y, float width, float height,
			float radius, Color fill, Color stroke) throws IOException {
		float control = radius * 0.55228475f;
		content.moveTo(x + radius, y);
		content.lineTo(x + width - radius, y);
		content.curveTo(x + width - radius + control, y, x + width, y + radius - control, x + width, y + radius);
		content.lineTo(x + width, y + height - radius);
		content.curveTo(x + width, y + height - radius + control, x + width - radius + control, y + height,
				x + width - radius, y + height);
		content.lineTo(x + radius, y + height);
		content.curveTo(x + radius - control, y + height, x, y + height - radius + control, x,
				y + height - radius);
		content.lineTo(x, y + radius);
		content.curveTo(x, y + radius - control, x + radius - control, y, x + radius, y);
		content.closePath();
		setNonStrokingColor(content, fill);
		setStrokingColor(content, stroke);
		content.fillAndStroke();
	}

	private void fillRect(PDPageContentStream content, float x, float y, float width, float height, Color color)
			throws IOException {
		setNonStrokingColor(content, color);
		content.addRect(x, y, width, height);
		content.fill();
	}

	private void fillCircle(PDPageContentStream content, float centerX, float centerY, float radius, Color color)
			throws IOException {
		float control = radius * 0.55228475f;
		content.moveTo(centerX + radius, centerY);
		content.curveTo(centerX + radius, centerY + control, centerX + control, centerY + radius, centerX,
				centerY + radius);
		content.curveTo(centerX - control, centerY + radius, centerX - radius, centerY + control, centerX - radius,
				centerY);
		content.curveTo(centerX - radius, centerY - control, centerX - control, centerY - radius, centerX,
				centerY - radius);
		content.curveTo(centerX + control, centerY - radius, centerX + radius, centerY - control, centerX + radius,
				centerY);
		content.closePath();
		setNonStrokingColor(content, color);
		content.fill();
	}

	private void writeText(PDPageContentStream content, String text, PDFont font, float size, float x, float y,
			Color color) throws IOException {
		content.beginText();
		content.setFont(font, size);
		setNonStrokingColor(content, color);
		content.newLineAtOffset(x, y);
		content.showText(supportedText(text, font));
		content.endText();
	}

	private void writeCenteredText(PDPageContentStream content, String text, PDFont font, float size, float x,
			float y, float width, Color color) throws IOException {
		String supported = supportedText(text, font);
		float textWidth = font.getStringWidth(supported) / 1000 * size;
		writeText(content, supported, font, size, x + Math.max(0, (width - textWidth) / 2), y, color);
	}

	private void writeRightText(PDPageContentStream content, String text, PDFont font, float size, float right,
			float y, Color color) throws IOException {
		String supported = supportedText(text, font);
		float textWidth = font.getStringWidth(supported) / 1000 * size;
		writeText(content, supported, font, size, right - textWidth, y, color);
	}

	private String truncate(String value, PDFont font, float size, float maxWidth) throws IOException {
		String normalized = supportedText(value, font);
		if (font.getStringWidth(normalized) / 1000 * size <= maxWidth) return normalized;
		String suffix = "...";
		while (!normalized.isEmpty() && font.getStringWidth(normalized + suffix) / 1000 * size > maxWidth) {
			normalized = normalized.substring(0, normalized.length() - 1);
		}
		return normalized + suffix;
	}

	private String supportedText(String value, PDFont font) {
		if (value == null) return "";
		String normalized = value.replace('\u00A0', ' ');
		StringBuilder sanitized = new StringBuilder();
		for (int offset = 0; offset < normalized.length();) {
			int codePoint = normalized.codePointAt(offset);
			String glyph = new String(Character.toChars(codePoint));
			try {
				font.encode(glyph);
				sanitized.append(glyph);
			} catch (IOException | IllegalArgumentException ex) {
				sanitized.append('?');
			}
			offset += Character.charCount(codePoint);
		}
		return sanitized.toString();
	}

	private String currency(BigDecimal value) {
		return NumberFormat.getCurrencyInstance(BRAZIL).format(value).replace('\u00A0', ' ');
	}

	private String percent(double value) {
		return String.format(BRAZIL, "%.1f%%", value);
	}

	private String expenseVariationLabel(BigDecimal current, BigDecimal previous) {
		if (previous.signum() == 0) return current.signum() == 0 ? "0,0%" : "Nova";
		double variation = current.subtract(previous).divide(previous, 6, java.math.RoundingMode.HALF_UP)
				.doubleValue() * 100;
		return String.format(BRAZIL, "%+.1f%%", variation);
	}

	private Color expenseVariationColor(BigDecimal current, BigDecimal previous) {
		if (previous.signum() == 0) return current.signum() == 0 ? SLATE_500 : BLUE;
		int comparison = current.compareTo(previous);
		if (comparison < 0) return EMERALD;
		if (comparison > 0) return ROSE;
		return SLATE_500;
	}

	private String periodLabel(LocalDate month) {
		return capitalize(month.getMonth().getDisplayName(TextStyle.FULL, BRAZIL)) + " de " + month.getYear();
	}

	private String capitalize(String value) {
		if (value == null || value.isBlank()) return "";
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}

	private void setNonStrokingColor(PDPageContentStream content, Color color) throws IOException {
		content.setNonStrokingColor(color.red() / 255f, color.green() / 255f, color.blue() / 255f);
	}

	private void setStrokingColor(PDPageContentStream content, Color color) throws IOException {
		content.setStrokingColor(color.red() / 255f, color.green() / 255f, color.blue() / 255f);
	}

	private record Color(int red, int green, int blue) {}

	private record Totals(BigDecimal income, BigDecimal expense, BigDecimal balance) {}

	private record CategoryTotal(String name, BigDecimal total) {}

	private record CategoryAnalysis(String name, BigDecimal currentTotal, BigDecimal previousTotal,
			double sharePercent, String largestDescription, BigDecimal largestAmount) {}

	private record PageContext(PDPageContentStream content, float y) implements AutoCloseable {
		@Override
		public void close() throws IOException {
			content.close();
		}
	}
}
