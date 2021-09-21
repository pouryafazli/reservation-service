package com.upgrade.reservation.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.upgrade.reservation.entity.ReservationDateEntity;
import com.upgrade.reservation.entity.ReservationEntity;
import com.upgrade.reservation.exception.DateIsNotAvailableException;
import com.upgrade.reservation.exception.ReservationNotFoundException;
import com.upgrade.reservation.filter.RequestCorrelation;
import com.upgrade.reservation.model.Reservation;
import com.upgrade.reservation.repository.ReservationDateRepository;
import com.upgrade.reservation.repository.ReservationRepository;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Setter
@Slf4j
@Transactional(isolation = Isolation.SERIALIZABLE)
public class ReservationServiceImpl implements ReservationService {

	@Autowired
	private ReservationRepository reservationRepository;
	@Autowired
	private ReservationDateRepository reservationDateRepository;

	@Override
	public List<LocalDate> ListAvailableDates(LocalDate startDate, LocalDate endDate) {

		startDate = startDate == null ? LocalDate.now() : startDate;
		endDate = endDate == null ? startDate.plusDays(30) : endDate;

		List<LocalDate> requestedDates = getDates(startDate, endDate).stream()
				.map(reservationDate -> reservationDate.getDate()).collect(Collectors.toList());
		List<LocalDate> reservedDates = reservationDateRepository.getDates(startDate, endDate).stream()
				.map(object -> object.getDate()).collect(Collectors.toList());
		log.info("Fetched reserved dates from database. [correlationId={}, availableDates ={}]",
				RequestCorrelation.getId(), reservedDates);
		requestedDates.removeAll(reservedDates);
		return requestedDates;
	}

	@Override
	public Reservation findById(long id) {
		log.info("Fetch the reservation. [correlationId={}, reservationId={}]", RequestCorrelation.getId(), id);
		return reservationRepository.findById(id).map(reservation -> mapToReservation(reservation))
				.orElseThrow(() -> new ReservationNotFoundException("Cannot find the reservation with id: " + id));
	}

	@Override
	public Reservation add(Reservation reservation) {
		log.info("Check if requested dates for resrvation is available. [correlationId={}]",
				RequestCorrelation.getId());
		List<ReservationDateEntity> reservedDates = reservationDateRepository.getDates(reservation.getStartDate(),
				reservation.getEndDate());
		// if we have a record in DB, then we cannot reserve for this dates
		if (!reservedDates.isEmpty()) {
			throw new DateIsNotAvailableException("Requested dates " + reservedDates + " are not available.");
		}

		List<ReservationDateEntity> dates = getDates(reservation.getStartDate(), reservation.getEndDate());
		log.info("Save/update reservation. [correlationId={}, reservationDates]", RequestCorrelation.getId(), dates);
		reservationDateRepository.saveAll(dates);

		ReservationEntity reservationEntity = mapToReservationtoEntity(reservation);
		reservationEntity = reservationRepository.save(reservationEntity);
		reservation.setId(reservationEntity.getId());
		return reservation;
	}

	@Override
	public Reservation update(Reservation reservation) {
		log.info("Fetch the reservation to update. [correlationId={}, reservationId={}]", RequestCorrelation.getId(),
				reservation.getId());
		Reservation reservationToUpdate = findById(reservation.getId());
		List<ReservationDateEntity> dates = getDates(reservationToUpdate.getStartDate(),
				reservationToUpdate.getEndDate());
		reservationDateRepository.deleteAll(dates);
		return add(reservation);
	}

	@Override
	public void delete(long id) {
		log.info("Fetch the reservation to delete. [correlationId={}, reservationId={}]", RequestCorrelation.getId(),
				id);
		Reservation reservation = findById(id);
		List<ReservationDateEntity> dates = getDates(reservation.getStartDate(), reservation.getEndDate());
		log.info("Delete the reservation. [correlationId={}, reservationId={}]", RequestCorrelation.getId(), id);
		reservationDateRepository.deleteAll(dates);
		reservationRepository.deleteById(id);
	}

	private Reservation mapToReservation(ReservationEntity reservationEntity) {
		return Reservation.builder().id(reservationEntity.getId()).firstName(reservationEntity.getFirstName())
				.lastName(reservationEntity.getLastName()).email(reservationEntity.getEmail())
				.startDate(reservationEntity.getStartDate()).endDate(reservationEntity.getEndDate()).build();
	}

	private ReservationEntity mapToReservationtoEntity(Reservation reservation) {

		ReservationEntity reservationEntity = ReservationEntity.builder().id(reservation.getId())
				.firstName(reservation.getFirstName()).lastName(reservation.getLastName()).email(reservation.getEmail())
				.startDate(reservation.getStartDate()).endDate(reservation.getEndDate()).build();

		return reservationEntity;
	}

	private List<ReservationDateEntity> getDates(LocalDate startDate, LocalDate endDate) {
		long numOfDays = ChronoUnit.DAYS.between(startDate, endDate);

		List<ReservationDateEntity> dateList = Stream.iterate(startDate, date -> date.plusDays(1)).limit(numOfDays)
				.map(date -> new ReservationDateEntity(date)).collect(Collectors.toList());
		return dateList;
	}

}
