package com.example.incomestatement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.incomestatement.IncomeStatement.Line;
import com.example.incomestatement.IncomeStatement.Section;

@Service
public class IncomeStatementService {

	/**
	 * PLACEHOLDER: returns made-up numbers so the response shape can be reviewed.
	 * The ledger is not read yet and the dates do not change the amounts.
	 */
	public IncomeStatement statement(LocalDate start, LocalDate end) {
		Section revenue = new Section(
				List.of(new Line("4000", "Product Revenue", new BigDecimal("1000.00"))),
				new BigDecimal("1000.00"));
		Section costOfGoodsSold = new Section(
				List.of(new Line("5000", "Cost of Goods Sold", new BigDecimal("400.00"))),
				new BigDecimal("400.00"));
		Section operatingExpenses = new Section(
				List.of(new Line("6000", "Salaries", new BigDecimal("250.00"))),
				new BigDecimal("250.00"));
		Section otherIncome = new Section(
				List.of(new Line("7000", "Interest Income", new BigDecimal("50.00"))),
				new BigDecimal("50.00"));

		return new IncomeStatement(
				start,
				end,
				revenue,
				costOfGoodsSold,
				new BigDecimal("600.00"),
				operatingExpenses,
				new BigDecimal("350.00"),
				otherIncome,
				new BigDecimal("400.00"));
	}

}
