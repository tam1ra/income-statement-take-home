package com.example.incomestatement;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class LedgerLoader {

	private final ObjectMapper objectMapper;
	private final Resource ledgerFile;

	public LedgerLoader(ObjectMapper objectMapper, @Value("${ledger.path}") Resource ledgerFile) {
		this.objectMapper = objectMapper;
		this.ledgerFile = ledgerFile;
	}

	/**
	 * Reads the ledger file. Debit and credit strings such as "12450.75" become BigDecimal
	 * directly, so no amount passes through a floating-point type.
	 */
	public Ledger load() {
		if (!ledgerFile.exists()) {
			throw new IllegalStateException("Ledger file not found: " + ledgerFile.getDescription());
		}
		try (InputStream in = ledgerFile.getInputStream()) {
			return objectMapper.readValue(in, Ledger.class);
		}
		catch (JacksonException | IOException e) {
			throw new IllegalStateException("Ledger file could not be read: " + ledgerFile.getDescription(), e);
		}
	}

}
