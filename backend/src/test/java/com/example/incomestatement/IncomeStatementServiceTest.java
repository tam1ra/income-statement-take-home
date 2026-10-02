package com.example.incomestatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.example.incomestatement.IncomeStatement.Line;

import tools.jackson.databind.json.JsonMapper;

/**
 * Runs the service against the real ledger.json. Every expected amount was added up by
 * hand from the journal entries named in the test, not copied from the service's output.
 */
class IncomeStatementServiceTest {

	private final IncomeStatementService service = new IncomeStatementService(
			new LedgerLoader(JsonMapper.builder().build(), new ClassPathResource("ledger.json")),
			new BankAccountLoader(JsonMapper.builder().build(), new ClassPathResource("bank.json"))
	);

	@Test
	void checkNotMatchBalance() {
//		assertThat(new IncomeStatementService(
//				new LedgerLoader(JsonMapper.builder().build(), new ClassPathResource("ledger.json")),
//				new BankAccountLoader(JsonMapper.builder().build(), new ClassPathResource("bank.json"))
//		)).

//		assertThat(new IncomeStatementService(
//				new LedgerLoader(JsonMapper.builder().build(), new ClassPathResource("ledger.json")),
//				new BankAccountLoader(JsonMapper.builder().build(), new ClassPathResource("bank.json"))
//		) -> assertThatThrownBy() )

		try {
			var serviceTest =new IncomeStatementService(
				new LedgerLoader(JsonMapper.builder().build(), new ClassPathResource("ledger.json")),
				new BankAccountLoader(JsonMapper.builder().build(), new ClassPathResource("broken_bank.json"))
			);
		} catch (Exception ex) {
			assertThat(ex).isInstanceOf(IllegalArgumentException.class);
		}
	}

	// ---------- The whole quarter ----------

	@Test
	void firstQuarter2026() {
		IncomeStatement statement = statement("2026-01-01", "2026-03-31");

		assertThat(statement.start()).isEqualTo(LocalDate.of(2026, 1, 1));
		assertThat(statement.end()).isEqualTo(LocalDate.of(2026, 3, 31));

		assertThat(statement.revenue().lines()).containsExactly(
				line("4000", "Product Revenue", "35650.75"), // 12450.75 + 8200.00 + 15000.00
				line("4100", "Subscription Revenue", "3000.00"), // 3 x 1000.00
				line("4900", "Sales Returns & Discounts", "-800.25")); // -(650.25 + 150.00)
		assertThat(statement.revenue().total()).isEqualTo(amount("37850.50"));

		assertThat(statement.costOfGoodsSold().lines()).containsExactly(
				line("5000", "Cost of Goods Sold", "14272.75")); // 4980.30 + 3280.00 + 6012.45
		assertThat(statement.costOfGoodsSold().total()).isEqualTo(amount("14272.75"));

		assertThat(statement.grossProfit()).isEqualTo(amount("23577.75"));

		assertThat(statement.operatingExpenses().lines()).containsExactly(
				line("6000", "Salaries", "55500.00"), // 3 x 18500.00, draft bonus excluded
				line("6100", "Rent", "9000.00"),
				line("6200", "Software", "1099.97"), // 1199.97 - 100.00 vendor credit
				line("6300", "Marketing (legacy)", "2500.10"));
		assertThat(statement.operatingExpenses().total()).isEqualTo(amount("68100.07"));

		assertThat(statement.operatingIncome()).isEqualTo(amount("-44522.32"));

		assertThat(statement.otherIncome().lines()).containsExactly(
				line("7000", "Interest Income", "42.18"));
		assertThat(statement.otherIncome().total()).isEqualTo(amount("42.18"));

		assertThat(statement.netIncome()).isEqualTo(amount("-44480.14"));
	}

	@Test
	void theThreeMonthsAddUpToTheQuarter() {
		BigDecimal january = statement("2026-01-01", "2026-01-31").netIncome();
		BigDecimal february = statement("2026-02-01", "2026-02-28").netIncome();
		BigDecimal march = statement("2026-03-01", "2026-03-31").netIncome();

		assertThat(january).isEqualTo(amount("-21529.65"));
		assertThat(february).isEqualTo(amount("-13230.25"));
		assertThat(march).isEqualTo(amount("-9720.24"));
		assertThat(january.add(february).add(march))
				.isEqualTo(statement("2026-01-01", "2026-03-31").netIncome());
	}

	// ---------- Entry status ----------

	@Test
	void voidEntryIsExcluded() {
		// JE-009 (void) and JE-010 (posted) are the same 8200.00 sale entered twice.
		IncomeStatement statement = statement("2026-02-03", "2026-02-03");

		assertThat(statement.revenue().lines()).containsExactly(line("4000", "Product Revenue", "8200.00"));
	}

	@Test
	void draftEntryIsExcluded() {
		// JE-019 is a draft 5000.00 bonus on 2026-03-15, the only salary entry that day.
		IncomeStatement statement = statement("2026-03-15", "2026-03-15");

		assertThat(statement.operatingExpenses().lines()).isEmpty();
		assertThat(statement.netIncome()).isEqualTo(amount("0.00"));
	}

	// ---------- Date range ----------

	@Test
	void startDateIsInclusive() {
		// JE-007 rent is dated 2026-01-01.
		assertThat(statement("2026-01-01", "2026-01-04").operatingExpenses().lines())
				.containsExactly(line("6100", "Rent", "9000.00"));
		assertThat(statement("2026-01-02", "2026-01-04").operatingExpenses().lines()).isEmpty();
	}

	@Test
	void endDateIsInclusive() {
		// JE-021, JE-022 and JE-023 are dated 2026-03-31.
		assertThat(statement("2026-03-21", "2026-03-31").netIncome()).isEqualTo(amount("-17457.82"));
		assertThat(statement("2026-03-21", "2026-03-30").netIncome()).isEqualTo(amount("0.00"));
	}

	@Test
	void singleDayRangeIncludesThatDay() {
		IncomeStatement statement = statement("2026-03-31", "2026-03-31");

		assertThat(statement.revenue().total()).isEqualTo(amount("1000.00"));
		assertThat(statement.operatingExpenses().total()).isEqualTo(amount("18500.00"));
		assertThat(statement.otherIncome().total()).isEqualTo(amount("42.18"));
		assertThat(statement.netIncome()).isEqualTo(amount("-17457.82")); // 1000.00 - 18500.00 + 42.18
	}

	@Test
	void entriesOutsideTheQuarterAreExcludedFromItAndIncludedInTheirOwnPeriod() {
		// JE-001 is dated 2025-12-15 and JE-024 is dated 2026-04-01.
		assertThat(statement("2025-12-01", "2025-12-31").netIncome()).isEqualTo(amount("5000.00"));
		assertThat(statement("2026-04-01", "2026-04-30").netIncome()).isEqualTo(amount("9100.00"));
		// 5000.00 - 44480.14 + 9100.00
		assertThat(statement("2025-12-15", "2026-04-01").netIncome()).isEqualTo(amount("-30380.14"));
	}

	// ---------- Accounts and signs ----------

	@Test
	void contraRevenueIsANegativeLineInsideRevenue() {
		// February: JE-010 sale 8200.00, JE-014 subscription 1000.00, JE-012 return 650.25.
		IncomeStatement statement = statement("2026-02-01", "2026-02-28");

		assertThat(statement.revenue().lines()).containsExactly(
				line("4000", "Product Revenue", "8200.00"),
				line("4100", "Subscription Revenue", "1000.00"),
				line("4900", "Sales Returns & Discounts", "-650.25"));
		assertThat(statement.revenue().total()).isEqualTo(amount("8549.75"));
	}

	@Test
	void entryWithThreeLinesPostsRevenueAndDiscountSeparately() {
		// JE-016: 15000.00 credit to revenue and a 150.00 debit to returns and discounts.
		IncomeStatement statement = statement("2026-03-02", "2026-03-02");

		assertThat(statement.revenue().lines()).containsExactly(
				line("4000", "Product Revenue", "15000.00"),
				line("4900", "Sales Returns & Discounts", "-150.00"));
		assertThat(statement.revenue().total()).isEqualTo(amount("14850.00"));
	}

	@Test
	void creditToAnExpenseAccountReducesTheExpense() {
		// JE-018 software 1199.97, then JE-020 vendor credit of 100.00.
		assertThat(statement("2026-03-10", "2026-03-20").operatingExpenses().lines())
				.containsExactly(line("6200", "Software", "1099.97"));
		// The credit on its own shows as a negative expense, which raises income.
		IncomeStatement creditOnly = statement("2026-03-20", "2026-03-20");
		assertThat(creditOnly.operatingExpenses().lines()).containsExactly(line("6200", "Software", "-100.00"));
		assertThat(creditOnly.netIncome()).isEqualTo(amount("100.00"));
	}

	@Test
	void otherIncomeIsNotPartOfRevenueOrOperatingIncome() {
		// March: account 7000 has type revenue but subtype other_income.
		IncomeStatement statement = statement("2026-03-01", "2026-03-31");

		assertThat(statement.revenue().total()).isEqualTo(amount("15850.00")); // 15000 + 1000 - 150
		assertThat(statement.operatingIncome()).isEqualTo(amount("-9762.42"));
		assertThat(statement.otherIncome().lines()).containsExactly(line("7000", "Interest Income", "42.18"));
		assertThat(statement.netIncome()).isEqualTo(amount("-9720.24"));
	}

	@Test
	void balanceSheetOnlyEntriesDoNotAppear() {
		// JE-004 (2026-01-10) bills a subscription to deferred revenue. JE-013 (2026-02-15)
		// is a cash receipt. Neither touches a revenue or expense account.
		assertNoActivity(statement("2026-01-10", "2026-01-10"));
		assertNoActivity(statement("2026-02-15", "2026-02-15"));
	}

	@Test
	void inactiveAccountWithActivityIsStillReported() {
		// JE-008 posts 2500.10 to 6300 Marketing (legacy), which is no longer active.
		IncomeStatement statement = statement("2026-01-20", "2026-01-20");

		assertThat(statement.operatingExpenses().lines())
				.containsExactly(line("6300", "Marketing (legacy)", "2500.10"));
	}

	@Test
	void rentIsReportedWhenRecordedNotSpreadOverTheQuarter() {
		// JE-007 expenses three months of rent on 2026-01-01.
		assertThat(statement("2026-01-01", "2026-01-31").operatingExpenses().lines())
				.contains(line("6100", "Rent", "9000.00"));
		assertThat(statement("2026-02-01", "2026-03-31").operatingExpenses().lines())
				.extracting(Line::accountNumber)
				.doesNotContain("6100");
	}

	@Test
	void accountsWithoutActivityInThePeriodHaveNoLine() {
		// January has no returns (4900), no software (6200) and no interest (7000).
		IncomeStatement statement = statement("2026-01-01", "2026-01-31");

		assertThat(statement.revenue().lines()).extracting(Line::accountNumber).containsExactly("4000", "4100");
		assertThat(statement.operatingExpenses().lines()).extracting(Line::accountNumber)
				.containsExactly("6000", "6100", "6300");
		assertThat(statement.otherIncome().lines()).isEmpty();
	}

	// ---------- Empty results ----------

	@Test
	void periodWithNoActivityHasEmptySectionsAndZeroTotals() {
		assertNoActivity(statement("2027-01-01", "2027-12-31"));
	}

	@Test
	void dayWithOnlyAVoidEntryHasNoActivity() {
		// JE-025 is void and dated 2026-03-18.
		assertNoActivity(statement("2026-03-18", "2026-03-18"));
	}

	private static void assertNoActivity(IncomeStatement statement) {
		assertThat(statement.revenue().lines()).isEmpty();
		assertThat(statement.costOfGoodsSold().lines()).isEmpty();
		assertThat(statement.operatingExpenses().lines()).isEmpty();
		assertThat(statement.otherIncome().lines()).isEmpty();
		// isEqualTo on BigDecimal also checks the scale, so these must be 0.00 and not 0.
		assertThat(statement.revenue().total()).isEqualTo(amount("0.00"));
		assertThat(statement.costOfGoodsSold().total()).isEqualTo(amount("0.00"));
		assertThat(statement.grossProfit()).isEqualTo(amount("0.00"));
		assertThat(statement.operatingExpenses().total()).isEqualTo(amount("0.00"));
		assertThat(statement.operatingIncome()).isEqualTo(amount("0.00"));
		assertThat(statement.otherIncome().total()).isEqualTo(amount("0.00"));
		assertThat(statement.netIncome()).isEqualTo(amount("0.00"));
	}

	private IncomeStatement statement(String start, String end) {
		return service.statement(LocalDate.parse(start), LocalDate.parse(end));
	}

	private static Line line(String accountNumber, String accountName, String amount) {
		return new Line(accountNumber, accountName, amount(amount));
	}

	private static BigDecimal amount(String value) {
		return new BigDecimal(value);
	}

}
