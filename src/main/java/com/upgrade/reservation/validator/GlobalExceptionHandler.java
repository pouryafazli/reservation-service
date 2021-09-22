package com.upgrade.reservation.validator;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.upgrade.reservation.exception.DateIsNotAvailableException;
import com.upgrade.reservation.exception.ReservationNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", new Date());
		body.put("status", status.value());

		// Get all errors
		List<String> errors = ex.getBindingResult().getAllErrors().stream().map(x -> x.getDefaultMessage())
				.collect(Collectors.toList());

		body.put("errors", errors);

		return new ResponseEntity<>(body, status);
	}

	@ExceptionHandler(DateIsNotAvailableException.class)
	ResponseEntity<Object> badRequestHandler(DateIsNotAvailableException e) {
		return buildResponse(HttpStatus.BAD_REQUEST, e);
	}

	@ExceptionHandler(ReservationNotFoundException.class)
	ResponseEntity<Object> badRequestHandler(ReservationNotFoundException e) {
		return buildResponse(HttpStatus.NOT_FOUND, e);
	}

	private ResponseEntity<Object> buildResponse(HttpStatus status, RuntimeException exception) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", new Date());
		body.put("status", status.value());
		body.put("message", exception.getMessage());
		return new ResponseEntity<>(body, status);
	}
}