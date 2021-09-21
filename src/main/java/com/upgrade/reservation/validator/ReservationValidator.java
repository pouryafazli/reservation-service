package com.upgrade.reservation.validator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.upgrade.reservation.model.Reservation;

public class ReservationValidator implements ConstraintValidator<ValidateReservation, Reservation> {

	public final static int MAX_RESERVATION_PERIOD = 3;
	public final static int MIN_DAYS_AHEAD_ARRIVAL = 1;
	public final static int MAX_DAYS_AHEAD_ARRIVAL = 30;

	@Override
	public boolean isValid(Reservation reservation, ConstraintValidatorContext context) {
		boolean isValid = true;

		if (reservation.getStartDate().isAfter(reservation.getEndDate())) {
			overrideDefaultErrorMessage("Reservation start date cannot be after the end date", context);
			isValid = false;
		}

		long numOfDays = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
		if (numOfDays > MAX_RESERVATION_PERIOD) {
			overrideDefaultErrorMessage(
					"Cannot reserve the campsite for more than " + MAX_RESERVATION_PERIOD + " days.", context);
			isValid = false;
		}

		long reservationDateAheadOfArrival = ChronoUnit.DAYS.between(LocalDate.now(), reservation.getStartDate());
		if (reservationDateAheadOfArrival < MIN_DAYS_AHEAD_ARRIVAL
				|| reservationDateAheadOfArrival > MAX_DAYS_AHEAD_ARRIVAL) {
			overrideDefaultErrorMessage(
					"The campsite can be reserved minimum " + MIN_DAYS_AHEAD_ARRIVAL
							+ " day(s) ahead of arrival and up to " + MAX_DAYS_AHEAD_ARRIVAL + " month in advance.",
					context);
			isValid = false;
		}

		return isValid;
	}

	private void overrideDefaultErrorMessage(String message, ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
	}

}
