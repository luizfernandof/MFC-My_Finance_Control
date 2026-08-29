package br.com.devl.mfc.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import br.com.devl.mfc.auth.entity.User;
import br.com.devl.mfc.entity.Transaction;
import br.com.devl.mfc.entity.TransactionGroup;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	@Query("""
			SELECT t FROM Transaction t
			WHERE t.user = :user
			AND MONTH(t.date) = :month
			AND YEAR(t.date) = :year
			AND (
				:search = ''
				OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))
				OR LOWER(t.category.name) LIKE LOWER(CONCAT('%', :search, '%'))
			)
			""")
	Page<Transaction> findByUserAndMonthAndYear(@Param("user") User user, @Param("month") int month,
			@Param("year") int year, @Param("search") String search, Pageable pageable);

	Optional<Transaction> findByIdAndUser(Long id, User user);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("UPDATE Transaction t SET t.active = false, t.updatedAt = :updatedAt WHERE t.group = :group AND t.user = :user")
	void deleteByGroupAndUser(@Param("group") TransactionGroup group, @Param("user") User user,
			@Param("updatedAt") Instant updatedAt);

	@Query("""
			SELECT t from Transaction t
			WHERE t.user = :user
			AND t.date BETWEEN :start AND :end
			""")
	List<Transaction> findByUserAndDateBetween(@Param("user") User user, @Param("start") LocalDate start,
			@Param("end") LocalDate end);

	@Query("""
			SELECT SUM(t.amount)
			FROM Transaction t
			WHERE t.type = 'INCOME'
			AND t.user = :user
			AND MONTH(t.date) = :month
			AND YEAR(t.date) = :year
			""")
	BigDecimal sumIncomeByMonth(@Param("user") User user, @Param("month") int month, @Param("year") int year);

	@Query("""
			SELECT SUM(t.amount)
			FROM Transaction t
			WHERE t.type = 'EXPENSE'
			AND t.user = :user
			AND MONTH(t.date) = :month
			AND YEAR(t.date) = :year
			""")
	BigDecimal sumExpenseByMonth(@Param("user") User user, @Param("month") int month, @Param("year") int year);

	@Query("""
			SELECT t.category.name, SUM(t.amount)
			FROM Transaction t
			WHERE t.type = 'EXPENSE'
			AND t.user = :user
			AND MONTH(t.date) = :month
			AND YEAR(t.date) = :year
			GROUP BY t.category.name
			""")
	List<Object[]> sumExpensesByCategory(@Param("user") User user, @Param("month") int month, @Param("year") int year);

	@Query("""
			SELECT t from Transaction t
			WHERE t.user = :user
			AND MONTH(t.date) = :month
			AND YEAR(t.date) = :year
			ORDER BY t.date ASC
			""")
	List<Transaction> findByUserAndMonthAndYearOrderByDate(@Param("user") User user, @Param("month") int month,
			@Param("year") int year);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("UPDATE Transaction t SET t.active = false, t.updatedAt = :updatedAt WHERE t.group = :group AND t.user = :user AND t.date >= :startDate")
	void deleteFromGroupOnwards(@Param("group") TransactionGroup group, @Param("user") User user,
			@Param("startDate") LocalDate startDate, @Param("updatedAt") Instant updatedAt);

	@Query("""
			SELECT MONTH(t.date), YEAR(t.date), SUM(t.amount)
			FROM Transaction t
			WHERE t.type = 'INCOME'
			AND t.user = :user
			AND t.date BETWEEN :start AND :end
			GROUP BY YEAR(t.date), MONTH(t.date)
			ORDER BY YEAR(t.date), MONTH(t.date)
			""")
	List<Object[]> sumIncomeByMonthRange(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

	@Query("""
			SELECT MONTH(t.date), YEAR(t.date), SUM(t.amount)
			FROM Transaction t
			WHERE t.type = 'EXPENSE'
			AND t.user = :user
			AND t.date BETWEEN :start AND :end
			GROUP BY YEAR(t.date), MONTH(t.date)
			ORDER BY YEAR(t.date), MONTH(t.date)
			""")
	List<Object[]> sumExpenseByMonthRange(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

}
