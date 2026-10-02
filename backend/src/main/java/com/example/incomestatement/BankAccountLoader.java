package com.example.incomestatement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

@Component
public class BankAccountLoader {

	private final ObjectMapper objectMapper;
	private final Resource bankFile;

	public BankAccountLoader(ObjectMapper objectMapper, @Value("${bank.path}") Resource bankFile) {
		this.objectMapper = objectMapper;
		this.bankFile = bankFile;
	}

	/**
	 * Reads the ledger file. Debit and credit strings such as "12450.75" become BigDecimal
	 * directly, so no amount passes through a floating-point type.
	 */
	public BankAccount load() {
		if (!bankFile.exists()) {
			throw new IllegalStateException("Bank file not found: " + bankFile.getDescription());
		}
		try (InputStream in = bankFile.getInputStream()) {
			return objectMapper.readValue(in, BankAccount.class);
		}
		catch (JacksonException | IOException e) {
			throw new IllegalStateException("Bank file could not be read: " + bankFile.getDescription(), e);
		}
	}

}
