package com.example.incomestatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.example.incomestatement.IncomeStatement.Line;
import com.example.incomestatement.IncomeStatement.Section;

@WebMvcTest(IncomeStatementController.class)
class IncomeStatementControllerTest {

	private static final LocalDate START = LocalDate.of(2026, 1, 1);
	private static final LocalDate END = LocalDate.of(2026, 3, 31);

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private IncomeStatementService service;

	@Test
	void returnsStatementForTheRequestedDates() {
		given(service.statement(START, END)).willReturn(sampleStatement(START, END));

		var response = mvc.get().uri("/income-statement?start=2026-01-01&end=2026-03-31");

		assertThat(response).hasStatusOk();
		assertThat(response).bodyJson().extractingPath("$.start").isEqualTo("2026-01-01");
		assertThat(response).bodyJson().extractingPath("$.end").isEqualTo("2026-03-31");
	}

	@Test
	void writesSectionsWithLinesAndTotals() {
		given(service.statement(START, END)).willReturn(sampleStatement(START, END));

		var response = mvc.get().uri("/income-statement?start=2026-01-01&end=2026-03-31");

		assertThat(response).bodyJson().extractingPath("$.revenue.lines[0].accountNumber").isEqualTo("4000");
		assertThat(response).bodyJson().extractingPath("$.revenue.lines[0].accountName").isEqualTo("Product Revenue");
		assertThat(response).bodyJson().extractingPath("$.revenue.lines[1].accountNumber").isEqualTo("4900");
		assertThat(response).bodyJson().extractingPath("$.costOfGoodsSold.lines[0].accountNumber").isEqualTo("5000");
		assertThat(response).bodyJson().extractingPath("$.operatingExpenses.lines[0].accountNumber").isEqualTo("6000");
		assertThat(response).bodyJson().extractingPath("$.otherIncome.lines").asArray().isEmpty();
	}

	@Test
	void writesAmountsAsStringsWithTwoDecimals() {
		given(service.statement(START, END)).willReturn(sampleStatement(START, END));

		var response = mvc.get().uri("/income-statement?start=2026-01-01&end=2026-03-31");

		assertThat(response).bodyJson().extractingPath("$.revenue.lines[0].amount").isEqualTo("1000.10");
		assertThat(response).bodyJson().extractingPath("$.revenue.lines[1].amount").isEqualTo("-100.00");
		assertThat(response).bodyJson().extractingPath("$.revenue.total").isEqualTo("900.10");
		assertThat(response).bodyJson().extractingPath("$.costOfGoodsSold.total").isEqualTo("400.00");
		assertThat(response).bodyJson().extractingPath("$.grossProfit").isEqualTo("500.10");
		assertThat(response).bodyJson().extractingPath("$.operatingExpenses.total").isEqualTo("750.00");
		assertThat(response).bodyJson().extractingPath("$.operatingIncome").isEqualTo("-249.90");
		assertThat(response).bodyJson().extractingPath("$.otherIncome.total").isEqualTo("0.00");
		assertThat(response).bodyJson().extractingPath("$.netIncome").isEqualTo("-249.90");
	}

	@Test
	void acceptsASingleDayRange() {
		LocalDate day = LocalDate.of(2026, 3, 31);
		given(service.statement(day, day)).willReturn(sampleStatement(day, day));

		var response = mvc.get().uri("/income-statement?start=2026-03-31&end=2026-03-31");

		assertThat(response).hasStatusOk();
		assertThat(response).bodyJson().extractingPath("$.start").isEqualTo("2026-03-31");
		assertThat(response).bodyJson().extractingPath("$.end").isEqualTo("2026-03-31");
	}

	@Test
	void missingStartIsBadRequest() {
		assertBadRequest("/income-statement?end=2026-03-31", "start is required (YYYY-MM-DD)");
	}

	@Test
	void missingEndIsBadRequest() {
		assertBadRequest("/income-statement?start=2026-01-01", "end is required (YYYY-MM-DD)");
	}

	@Test
	void emptyStartIsBadRequest() {
		assertBadRequest("/income-statement?start=&end=2026-03-31", "start is required (YYYY-MM-DD)");
	}

	@Test
	void impossibleDateIsBadRequest() {
		assertBadRequest("/income-statement?start=2026-02-30&end=2026-03-31",
				"start is not a valid date (YYYY-MM-DD): 2026-02-30");
	}

	@Test
	void wrongDateFormatIsBadRequest() {
		assertBadRequest("/income-statement?start=2026-01-01&end=03/31/2026",
				"end is not a valid date (YYYY-MM-DD): 03/31/2026");
	}

	@Test
	void startAfterEndIsBadRequest() {
		assertBadRequest("/income-statement?start=2026-03-31&end=2026-01-01", "start must not be after end");
	}

	private void assertBadRequest(String uri, String message) {
		var response = mvc.get().uri(uri);

		assertThat(response).hasStatus(400);
		assertThat(response).bodyJson().extractingPath("$.message").isEqualTo(message);
		verifyNoInteractions(service);
	}

	private static IncomeStatement sampleStatement(LocalDate start, LocalDate end) {
		return new IncomeStatement(
				start,
				end,
				new Section(
						List.of(
								new Line("4000", "Product Revenue", new BigDecimal("1000.10")),
								new Line("4900", "Sales Returns & Discounts", new BigDecimal("-100.00"))),
						new BigDecimal("900.10")),
				new Section(
						List.of(new Line("5000", "Cost of Goods Sold", new BigDecimal("400.00"))),
						new BigDecimal("400.00")),
				new BigDecimal("500.10"),
				new Section(
						List.of(new Line("6000", "Salaries", new BigDecimal("750.00"))),
						new BigDecimal("750.00")),
				new BigDecimal("-249.90"),
				new Section(List.of(), new BigDecimal("0.00")),
				new BigDecimal("-249.90"));
	}

}
