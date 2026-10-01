package com.example.incomestatement;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class IncomeStatementServiceTest {

	private final IncomeStatementService service = new IncomeStatementService();

	@Test
	void statementCarriesTheRequestedDates() {
		LocalDate start = LocalDate.of(2026, 1, 1);
		LocalDate end = LocalDate.of(2026, 3, 31);

		IncomeStatement statement = service.statement(start, end);

		assertThat(statement.start()).isEqualTo(start);
		assertThat(statement.end()).isEqualTo(end);
	}

	@Test
	void subtotalsFollowTheStatementFormulas() {
		IncomeStatement statement = service.statement(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));

		assertThat(statement.grossProfit())
				.isEqualTo(statement.revenue().total().subtract(statement.costOfGoodsSold().total()));
		assertThat(statement.operatingIncome())
				.isEqualTo(statement.grossProfit().subtract(statement.operatingExpenses().total()));
		assertThat(statement.netIncome())
				.isEqualTo(statement.operatingIncome().add(statement.otherIncome().total()));
	}

}
