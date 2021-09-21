package com.upgrade.reservation.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = {ReservationValidator.class})
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateReservation {
	
	String message() default "default message";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default{};
	
}
