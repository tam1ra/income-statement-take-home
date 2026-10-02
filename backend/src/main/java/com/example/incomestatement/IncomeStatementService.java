package com.example.incomestatement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.incomestatement.IncomeStatement.Line;
import com.example.incomestatement.IncomeStatement.Section;
import com.example.incomestatement.Ledger.Account;
import com.example.incomestatement.Ledger.JournalEntry;
import com.example.incomestatement.Ledger.JournalLine;

@Service
public class IncomeStatementService {

	private static final BigDecimal ZERO = new BigDecimal("0.00");

	private final Ledger ledger;
	private final BankAccount bankAccount;
	private final Map<String, Account> accountsByNumber = new HashMap<>();

	public IncomeStatementService(LedgerLoader ledgerLoader, BankAccountLoader bankAccountLoader) {
		this.ledger = ledgerLoader.load();
		this.bankAccount = bankAccountLoader.load();

		List<Account> cash = new ArrayList<>();
		for (Account account : ledger.accounts()) {
			accountsByNumber.put(account.number(), account);
		}

		boolean match = true;
		for (var journal : ledger.journalEntries()) {
			if (journal.status().equals("POSTED")) {
				for (var line : journal.lines()) {
					if (line.account() == "1000") {
						for (var account : bankAccount.bankAccounts()) {
							if (!line.debit().equals(account.amount()) && !line.credit().equals(account.amount())) {
								throw new IllegalArgumentException("Cash is not match with bank balance");
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Builds the income statement for the entries dated from start to end, both inclusive.
	 */
	public IncomeStatement statement(LocalDate start, LocalDate end) {
		Map<String, BigDecimal> amounts = amountsByAccount(start, end);

		Section revenue = section(amounts, "operating_revenue", "contra_revenue");
		Section costOfGoodsSold = section(amounts, "cogs");
		Section operatingExpenses = section(amounts, "operating_expense");
		Section otherIncome = section(amounts, "other_income");

		BigDecimal grossProfit = revenue.total().subtract(costOfGoodsSold.total());
		BigDecimal operatingIncome = grossProfit.subtract(operatingExpenses.total());
		BigDecimal netIncome = operatingIncome.add(otherIncome.total());

		return new IncomeStatement(
				start,
				end,
				revenue,
				costOfGoodsSold,
				grossProfit,
				operatingExpenses,
				operatingIncome,
				otherIncome,
				netIncome);
	}

	/**
	 * Adds up the activity of each income statement account in the period. Only accounts
	 * that have at least one posted line in the period get an entry in the map.
	 */
	private Map<String, BigDecimal> amountsByAccount(LocalDate start, LocalDate end) {
		Map<String, BigDecimal> amounts = new HashMap<>();
		for (JournalEntry entry : ledger.journalEntries()) {
			if (!"posted".equals(entry.status())) {
				continue; // draft and void entries are not in the books
			}
			if (entry.date().isBefore(start) || entry.date().isAfter(end)) {
				continue;
			}
			for (JournalLine line : entry.lines()) {
				Account account = accountsByNumber.get(line.account());
				if (account == null) {
					throw new IllegalStateException(
							"Entry " + entry.id() + " uses unknown account " + line.account());
				}
				if ("balance_sheet".equals(account.subtype())) {
					continue;
				}
				amounts.merge(account.number(), signedAmount(account, line), BigDecimal::add);
			}
		}
		return amounts;
	}

	/**
	 * Expenses grow with debits, so debit minus credit. Revenue grows with credits, so
	 * credit minus debit. A sales return (a debit to a revenue account) comes out negative.
	 */
	private static BigDecimal signedAmount(Account account, JournalLine line) {
		if ("expense".equals(account.type())) {
			return line.debit().subtract(line.credit());
		}
		return line.credit().subtract(line.debit());
	}

	/**
	 * One line per account of the given subtypes that had activity, in chart of accounts order.
	 */
	private Section section(Map<String, BigDecimal> amounts, String... subtypes) {
		List<Line> lines = new ArrayList<>();
		BigDecimal total = ZERO;
		for (Account account : ledger.accounts()) {
			BigDecimal amount = amounts.get(account.number());
			if (amount == null || !List.of(subtypes).contains(account.subtype())) {
				continue;
			}
			lines.add(new Line(account.number(), account.name(), amount));
			total = total.add(amount);
		}
		return new Section(lines, total);
	}

}
