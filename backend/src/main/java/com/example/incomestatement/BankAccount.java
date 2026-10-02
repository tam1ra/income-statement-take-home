package com.example.incomestatement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * The contents of ledger.json: the chart of accounts and the journal entries.
 */
public record BankAccount(
		List<Account> bankAccounts) {

	public record Account(
			String id,
			String date,
			BigDecimal amount,
			String description) {
	}
}
