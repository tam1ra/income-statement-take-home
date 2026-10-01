package com.example.incomestatement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The contents of ledger.json: the chart of accounts and the journal entries.
 */
public record Ledger(
		String company,
		String currency,
		List<Account> accounts,
		@JsonProperty("journal_entries") List<JournalEntry> journalEntries) {

	public record Account(
			String number,
			String name,
			String type,
			String subtype,
			@JsonProperty("is_active") boolean isActive) {
	}

	public record JournalEntry(
			String id,
			LocalDate date,
			String status,
			String memo,
			List<JournalLine> lines) {
	}

	public record JournalLine(
			String account,
			BigDecimal debit,
			BigDecimal credit) {
	}

}
