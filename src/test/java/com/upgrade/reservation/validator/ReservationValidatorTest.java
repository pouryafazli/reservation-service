package com.upgrade.reservation.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.upgrade.reservation.model.Reservation;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class ReservationValidatorTest {
	ReservationValidator validator = new ReservationValidator();

	@Mock
	ConstraintValidatorContext context;
	@Mock
	ConstraintViolationBuilder constraintViolationBuilder;

	@BeforeEach
	void setUp() {
		Mockito.doNothing().when(context).disableDefaultConstraintViolation();
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
		when(constraintViolationBuilder.addConstraintViolation()).thenReturn(null);

	}

	@Test
	void testStartDateIsAfterEndDate() {

		Reservation reservation = getBaseReservation();
		reservation.setStartDate(LocalDate.now().plusDays(3));
		reservation.setEndDate(LocalDate.now().plusDays(1));
		assertThat(validator.isValid(reservation, context)).isFalse();
	}

	@Test
	void testCannotReserveForMoreThanLimit() {
		LocalDate startDate = LocalDate.now().plusDays(1);
		Reservation reservation = getBaseReservation();
		reservation.setStartDate(startDate);
		reservation.setEndDate(startDate.plusDays(ReservationValidator.MAX_RESERVATION_PERIOD + 1));
		assertThat(validator.isValid(reservation, context)).isFalse();
	}
	
	@Test
	void testCannotReserveTooEarly() {
		LocalDate startDate = LocalDate.now().plusDays(ReservationValidator.MAX_DAYS_AHEAD_ARRIVAL + 1);
		Reservation reservation = getBaseReservation();
		reservation.setStartDate(startDate);
		reservation.setEndDate(startDate.plusDays(ReservationValidator.MAX_RESERVATION_PERIOD));
		assertThat(validator.isValid(reservation, context)).isFalse();
	}

	@Test
	void testCannotReserveTooLate() {
		LocalDate startDate = LocalDate.now();
		Reservation reservation = getBaseReservation();
		reservation.setStartDate(startDate);
		reservation.setEndDate(startDate.plusDays(ReservationValidator.MAX_RESERVATION_PERIOD));
		assertThat(validator.isValid(reservation, context)).isFalse();
	}
	
	@Test
	void testCanReserveWithValidDate() {
		LocalDate startDate = LocalDate.now().plusDays(ReservationValidator.MIN_DAYS_AHEAD_ARRIVAL + 1);
		Reservation reservation = getBaseReservation();
		reservation.setStartDate(startDate);
		reservation.setEndDate(startDate.plusDays(ReservationValidator.MAX_RESERVATION_PERIOD));
		assertThat(validator.isValid(reservation, context)).isTrue();
	}
	
	private Reservation getBaseReservation() {
		return Reservation.builder().firstName("firstName").lastName("lastName").email("test@email.com")
				.startDate(LocalDate.now()).endDate(LocalDate.now()).build();
	}

}
