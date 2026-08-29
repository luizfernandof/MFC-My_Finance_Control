package br.com.devl.mfc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.DriverManager;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class PostgresMigrationIntegrationTest {

	@Container
	private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15");

	@Test
	void appliesEveryMigrationToPostgres() throws Exception {
		var migrationsThroughV6 = Flyway.configure()
				.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.locations("classpath:db/migration")
				.target("6")
				.load()
				.migrate();
		assertThat(migrationsThroughV6.success).isTrue();
		assertThat(migrationsThroughV6.migrationsExecuted).isEqualTo(6);

		// Reproduce the relevant differences found in the production V1 baseline:
		// Hibernate named this constraint and created expiry_date as TIMESTAMPTZ.
		try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
				POSTGRES.getPassword()); var statement = connection.createStatement()) {
			statement.execute("ALTER TABLE users RENAME CONSTRAINT uc_users_email TO uk6dotkott2kjsp8vw4d0m25fb7");
			statement.execute("""
					ALTER TABLE refresh_tokens
					ALTER COLUMN expiry_date TYPE TIMESTAMPTZ
					USING expiry_date AT TIME ZONE 'UTC'
					""");
		}

		var productionShapedMigration = Flyway.configure()
				.dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
				.locations("classpath:db/migration")
				.load()
				.migrate();
		assertThat(productionShapedMigration.success).isTrue();
		assertThat(productionShapedMigration.migrationsExecuted).isEqualTo(1);

		try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(),
				POSTGRES.getPassword());
				var statement = connection.prepareStatement("""
						SELECT
						    (SELECT data_type
						     FROM information_schema.columns
						     WHERE table_schema = 'public'
						       AND table_name = 'refresh_tokens'
						       AND column_name = 'expiry_date') AS expiry_date_type,
						    to_regclass('public.uc_users_email_ci') IS NOT NULL AS user_index_exists,
						    to_regclass('public.uc_categories_name_user_ci') IS NOT NULL AS category_index_exists,
						    NOT EXISTS (
						        SELECT 1
						        FROM pg_constraint
						        WHERE conrelid = 'users'::regclass
						          AND conname = 'uk6dotkott2kjsp8vw4d0m25fb7'
						    ) AS production_constraint_removed
						""");
				var rows = statement.executeQuery()) {
			assertThat(rows.next()).isTrue();
			assertThat(rows.getString("expiry_date_type")).isEqualTo("timestamp with time zone");
			assertThat(rows.getBoolean("user_index_exists")).isTrue();
			assertThat(rows.getBoolean("category_index_exists")).isTrue();
			assertThat(rows.getBoolean("production_constraint_removed")).isTrue();
		}
	}
}
