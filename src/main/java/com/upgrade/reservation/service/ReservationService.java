package com.upgrade.reservation.service;

import java.time.LocalDate;
import java.util.List;

import com.upgrade.reservation.model.Reservation;

public interface ReservationService {
	
	Reservation add(Reservation reservationEntity);
	Reservation update(Reservation reservationEntity);
	Reservation findById(long id);
	void delete(long id);
	List<LocalDate> ListAvailableDates(LocalDate startDate, LocalDate endDate);
	
}
