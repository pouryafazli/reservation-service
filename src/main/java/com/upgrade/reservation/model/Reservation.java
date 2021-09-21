package com.upgrade.reservation.model;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.upgrade.reservation.validator.ValidateReservation;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@ValidateReservation
public class Reservation {

	long id;

	@NotEmpty(message = "First Name may not be blank")
	private String firstName;
	@NotEmpty(message = "Last Name may not be blank")
	private String lastName;
	@Email(message = "Email address is not valid")
	@NotEmpty(message = "Email may not be blank")
	private String email;
	@Future(message = "Reservation start date is not in future")
	@NotNull(message = "Reservation start date may not be null")
	private LocalDate startDate;
	@Future(message = "Reservation end date is not in future")
	@NotNull(message = "Reservation end date may not be null")
	private LocalDate endDate;
}
