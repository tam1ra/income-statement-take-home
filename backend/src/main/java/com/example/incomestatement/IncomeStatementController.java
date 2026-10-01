package com.example.incomestatement;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IncomeStatementController {

	private final IncomeStatementService service;

	public IncomeStatementController(IncomeStatementService service) {
		this.service = service;
	}

	@GetMapping("/income-statement")
	public IncomeStatement incomeStatement(
			@RequestParam(required = false) String start,
			@RequestParam(required = false) String end) {
		LocalDate startDate = parseDate("start", start);
		LocalDate endDate = parseDate("end", end);
		if (startDate.isAfter(endDate)) {
			throw new BadRequestException("start must not be after end");
		}
		return service.statement(startDate, endDate);
	}

	private static LocalDate parseDate(String name, String value) {
		if (value == null || value.isBlank()) {
			throw new BadRequestException(name + " is required (YYYY-MM-DD)");
		}
		try {
			return LocalDate.parse(value);
		}
		catch (DateTimeParseException e) {
			throw new BadRequestException(name + " is not a valid date (YYYY-MM-DD): " + value);
		}
	}

	@ExceptionHandler(BadRequestException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse badRequest(BadRequestException e) {
		return new ErrorResponse(e.getMessage());
	}

	public record ErrorResponse(String message) {
	}

	private static class BadRequestException extends RuntimeException {

		BadRequestException(String message) {
			super(message);
		}

	}

}
