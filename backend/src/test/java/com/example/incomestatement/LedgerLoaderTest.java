package com.example.incomestatement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import com.example.incomestatement.Ledger.Account;
import com.example.incomestatement.Ledger.JournalEntry;
import com.example.incomestatement.Ledger.JournalLine;

import tools.jackson.databind.json.JsonMapper;

class LedgerLoaderTest {

	private final Ledger ledger = loader("ledger.json").load();

	@Test
	void loadsCompanyAndCurrency() {
		assertThat(ledger.company()).isEqualTo("Northwind Coffee Roasters");
		assertThat(ledger.currency()).isEqualTo("USD");
	}

	@Test
	void loadsEveryAccountAndJournalEntry() {
		assertThat(ledger.accounts()).hasSize(15);
		assertThat(ledger.journalEntries()).hasSize(25);
	}

	@Test
	void loadsAccountFields() {
		assertThat(ledger.accounts()).contains(
				new Account("4900", "Sales Returns & Discounts", "revenue", "contra_revenue", true),
				new Account("6300", "Marketing (legacy)", "expense", "operating_expense", false));
	}

	@Test
	void loadsJournalEntryFields() {
		JournalEntry entry = entry("JE-002");

		assertThat(entry.date()).isEqualTo(LocalDate.of(2026, 1, 5));
		assertThat(entry.status()).isEqualTo("posted");
		assertThat(entry.memo()).isEqualTo("January product sales");
	}

	@Test
	void loadsAmountsExactlyWithTwoDecimals() {
		assertThat(entry("JE-002").lines()).containsExactly(
				new JournalLine("1100", new BigDecimal("12450.75"), new BigDecimal("0.00")),
				new JournalLine("4000", new BigDecimal("0.00"), new BigDecimal("12450.75")));
	}

	@Test
	void loadsEntriesWithMoreThanTwoLines() {
		assertThat(entry("JE-016").lines()).hasSize(3);
	}

	@Test
	void loadsDraftAndVoidEntriesSoTheServiceDecidesWhatCounts() {
		assertThat(entry("JE-009").status()).isEqualTo("void");
		assertThat(entry("JE-019").status()).isEqualTo("draft");
	}

	@Test
	void missingFileFailsWithItsName() {
		LedgerLoader loader = loader("no-such-ledger.json");

		assertThatThrownBy(loader::load)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Ledger file not found: class path resource [no-such-ledger.json]");
	}

	@Test
	void malformedFileFailsWithAClearMessage() {
		// src/test/resources/broken-ledger.json is not valid JSON.
		LedgerLoader loader = loader("broken-ledger.json");

		assertThatThrownBy(loader::load)
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("Ledger file could not be read: class path resource [broken-ledger.json]");
	}

	private static LedgerLoader loader(String classpathFile) {
		return new LedgerLoader(JsonMapper.builder().build(), new ClassPathResource(classpathFile));
	}

	private JournalEntry entry(String id) {
		return ledger.journalEntries().stream()
				.filter(entry -> entry.id().equals(id))
				.findFirst()
				.orElseThrow();
	}

}
