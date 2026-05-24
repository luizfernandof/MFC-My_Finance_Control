package br.com.devl.mfc.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.auth.repository.UserRepository;
import br.com.devl.mfc.dto.CategoryExpenseDTO;
import br.com.devl.mfc.dto.DashboardSummaryDTO;
import br.com.devl.mfc.dto.MonthlyTrendDTO;
import br.com.devl.mfc.repository.TransactionRepository;

@Service
public class DashboardService {

	private final TransactionRepository repository;
	private final UserRepository userRepository;

	public DashboardService(TransactionRepository repository, UserRepository userRepository) {
		this.repository = repository;
		this.userRepository = userRepository;
	}

	private User getAuthenticatedUser() {
		return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}

	public DashboardSummaryDTO getSummary(int month, int year) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

		BigDecimal rawIncome = repository.sumIncomeByMonth(user, month, year);
		BigDecimal rawExpense = repository.sumExpenseByMonth(user, month, year);

		BigDecimal income = (rawIncome != null) ? rawIncome : BigDecimal.ZERO;
		BigDecimal expense = (rawExpense != null) ? rawExpense : BigDecimal.ZERO;
		BigDecimal balance = income.subtract(expense);

		List<Object[]> result = repository.sumExpensesByCategory(user, month, year);
		List<CategoryExpenseDTO> byCategory = (result == null) ? List.of()
				: result.stream().map(obj -> new CategoryExpenseDTO((String) obj[0],
						(BigDecimal) (obj[1] != null ? obj[1] : BigDecimal.ZERO))).toList();

		int prevMonth = month == 1 ? 12 : month - 1;
		int prevYear = month == 1 ? year - 1 : year;
		BigDecimal rawPrevIncome = repository.sumIncomeByMonth(user, prevMonth, prevYear);
		BigDecimal rawPrevExpense = repository.sumExpenseByMonth(user, prevMonth, prevYear);

		BigDecimal previousIncome = (rawPrevIncome != null) ? rawPrevIncome : BigDecimal.ZERO;
		BigDecimal previousExpense = (rawPrevExpense != null) ? rawPrevExpense : BigDecimal.ZERO;
		BigDecimal previousBalance = previousIncome.subtract(previousExpense);

		return new DashboardSummaryDTO(income, expense, balance, byCategory, previousIncome, previousExpense, previousBalance);
	}

	public List<MonthlyTrendDTO> getMonthlyTrend(int month, int year) {
		User user = getAuthenticatedUser();

		LocalDate endDate = YearMonth.of(year, month).atEndOfMonth();
		LocalDate startDate = YearMonth.of(year, month).minusMonths(5).atDay(1);

		List<Object[]> incomeResults = repository.sumIncomeByMonthRange(user, startDate, endDate);
		List<Object[]> expenseResults = repository.sumExpenseByMonthRange(user, startDate, endDate);

		Map<String, BigDecimal> incomeMap = new HashMap<>();
		for (Object[] row : incomeResults) {
			int m = ((Number) row[0]).intValue();
			int y = ((Number) row[1]).intValue();
			BigDecimal total = (BigDecimal) row[2];
			incomeMap.put(y + "-" + m, total != null ? total : BigDecimal.ZERO);
		}

		Map<String, BigDecimal> expenseMap = new HashMap<>();
		for (Object[] row : expenseResults) {
			int m = ((Number) row[0]).intValue();
			int y = ((Number) row[1]).intValue();
			BigDecimal total = (BigDecimal) row[2];
			expenseMap.put(y + "-" + m, total != null ? total : BigDecimal.ZERO);
		}

		List<MonthlyTrendDTO> trend = new java.util.ArrayList<>();
		for (int i = 5; i >= 0; i--) {
			YearMonth ym = YearMonth.of(year, month).minusMonths(i);
			String key = ym.getYear() + "-" + ym.getMonthValue();
			trend.add(new MonthlyTrendDTO(
					ym.getMonthValue(),
					ym.getYear(),
					incomeMap.getOrDefault(key, BigDecimal.ZERO),
					expenseMap.getOrDefault(key, BigDecimal.ZERO)));
		}

		return trend;
	}
}