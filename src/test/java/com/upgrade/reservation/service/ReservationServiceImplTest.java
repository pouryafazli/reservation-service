package com.upgrade.reservation.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.upgrade.reservation.entity.ReservationDateEntity;
import com.upgrade.reservation.entity.ReservationEntity;
import com.upgrade.reservation.exception.DateIsNotAvailableException;
import com.upgrade.reservation.model.Reservation;
import com.upgrade.reservation.repository.ReservationDateRepository;
import com.upgrade.reservation.repository.ReservationRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class ReservationServiceImplTest {

	ReservationServiceImpl reservationService = new ReservationServiceImpl();
	
	@Mock
	ReservationRepository reservationRepository;
	@Mock
	ReservationDateRepository reservationDateRepository;
	
	@BeforeEach
	void setUp() {
		reservationService.setReservationDateRepository(reservationDateRepository);
		reservationService.setReservationRepository(reservationRepository);
	}
	
	
	@Test
	void testAddReservationWithDateConflict() {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(2);
		List<ReservationDateEntity> reservedDates = loadDefaultDates(startDate,endDate);
		when(reservationDateRepository.getDates(any(LocalDate.class),any(LocalDate.class))).thenReturn(reservedDates);
		Reservation reservation = getBaseReservation();
		
		reservation.setStartDate(startDate);
		reservation.setEndDate(endDate);
		assertThrows(DateIsNotAvailableException.class, () -> reservationService.add(reservation));
	}

	@Test
	void testAddReservationWithNoDateConflict() {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(2);
		Reservation reservation = getBaseReservation();
		reservation.setStartDate(startDate);
		reservation.setEndDate(endDate);
		
		when(reservationDateRepository.getDates(any(LocalDate.class),any(LocalDate.class))).thenReturn(Collections.emptyList());
		ReservationEntity reservationEntity = createReservationEntity(reservation);
		when(reservationRepository.save(reservationEntity)).thenReturn(reservationEntity);
		
		reservationService.add(reservation);
		verify(reservationDateRepository).getDates(any(LocalDate.class),any(LocalDate.class));
		verify(reservationDateRepository).saveAll(any());
		verify(reservationRepository).save(any(ReservationEntity.class));
	}

	private Reservation getBaseReservation() {
		return Reservation.builder().firstName("firstName").lastName("lastName").email("test@email.com")
				.startDate(LocalDate.now()).endDate(LocalDate.now()).build();
	}
	
	private List<ReservationDateEntity> loadDefaultDates(LocalDate startDate, LocalDate endDate) {
		long numOfDays = ChronoUnit.DAYS.between(startDate, endDate);

		List<ReservationDateEntity> dateList = Stream.iterate(startDate, date -> date.plusDays(1)).limit(numOfDays)
				.map(date -> new ReservationDateEntity(date)).collect(Collectors.toList());
		return dateList;
	}
	
	private ReservationEntity createReservationEntity(Reservation reservation) {
		ReservationEntity reservationEntity = ReservationEntity.builder().id(reservation.getId())
				.firstName(reservation.getFirstName()).lastName(reservation.getLastName()).email(reservation.getEmail())
				.startDate(reservation.getStartDate()).endDate(reservation.getEndDate()).build();

		return reservationEntity;
	}


}
