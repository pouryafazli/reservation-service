package com.upgrade.reservation.resource;

import java.time.LocalDate;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.upgrade.reservation.exception.DateIsNotAvailableException;
import com.upgrade.reservation.exception.ReservationNotFoundException;
import com.upgrade.reservation.filter.RequestCorrelation;
import com.upgrade.reservation.model.Reservation;
import com.upgrade.reservation.service.ReservationService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@OpenAPIDefinition(info = @Info(title = "Campsite Reservation System"))
@Slf4j
@RestController
@RequestMapping("/reservations")
public class ReservationResource {

	@Autowired
	ReservationService reservationService;

	@Value("${spring.host}")
	private String host;

	@Operation(summary = "Retrieves available date for reservation." + "ation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The available dates has been fetched and is transmitted in the message body", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "server cannot process the request due to client error", content = @Content(mediaType = "application/json")) })
	@GetMapping("/available")
	public List<LocalDate> listAvailibility(
			@RequestParam(name = "startDate", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
			@RequestParam(name = "endDate", required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate) {

		log.info("start available dates request. [correlationId={}]", RequestCorrelation.getId());

		List<LocalDate> availableDates = reservationService.ListAvailableDates(startDate, endDate);
		log.info("completed available dates request. [correlationId={}]", RequestCorrelation.getId());
		return availableDates;
	}

	@Operation(summary = "Retrieves reservation from the server.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The reservation has been fetched and is transmitted in the message body", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "Server cannot find the requested reservation", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "server cannot process the request due to client error", content = @Content(mediaType = "application/json")) })
	@GetMapping("/{id}")
	public ResponseEntity<Reservation> getReservation(@PathVariable long id) {
		try {
			log.info("Start Get reservation request. [correlationId={}, reservationId{}]", RequestCorrelation.getId(),
					id);
			Reservation reservation = reservationService.findById(id);
			log.info("End Get reservation request. [correlationId={}, reservationId{}]", RequestCorrelation.getId(),
					id);
			return new ResponseEntity<>(reservation, HttpStatus.OK);
		} catch (ReservationNotFoundException e) {
			log.info("Cannot find the ereservation. [correlationId={}, reservationId{}]", RequestCorrelation.getId(),
					id);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (Exception e) {
			log.debug("Cannot complete the request. [correlationId={}, reservationId{}]", RequestCorrelation.getId(),
					id);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot complete", e);
		}
	}

	@Operation(summary = "Add a new Reservation.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "New reservation hs created and the new reservation is returned in the body of the message.", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "Server cannot process the request due to client error", content = @Content(mediaType = "application/json")) })
	@PostMapping("")
	public ResponseEntity<Reservation> createReservation(@Valid @RequestBody Reservation reservation) {
		try {
			log.info("Start create reservation request. [correlationId={}]", RequestCorrelation.getId());
			Reservation newReservation = reservationService.add(reservation);
			MultiValueMap<String, String> headers = new HttpHeaders();
			headers.add("Location", host + "/reservations/" + newReservation.getId());
			ResponseEntity<Reservation> response = new ResponseEntity<>(newReservation, headers, HttpStatus.CREATED);
			log.info("End create reservation request. [correlationId={}]", RequestCorrelation.getId());
			return response;
		} catch (DateIsNotAvailableException e) {
			log.debug("Requested dates are not available. [correlationId={}, startDate{}, enddate{}]",
					RequestCorrelation.getId(), reservation.getStartDate(), reservation.getEndDate());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campsite is not available for rquested time", e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot complete", e);
		}
	}

	@Operation(summary = "Update the reservation.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "request has succeeded and the reservation has updated", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "Server cannot find the requested reservation to update", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "server cannot process the request due to client error", content = @Content(mediaType = "application/json")) })
	@PutMapping("/{id}")
	public ResponseEntity<Reservation> updateReservation(@PathVariable long id,
			@Valid @RequestBody Reservation reservation) {
		try {
			reservation.setId(id);
			Reservation newReservation = reservationService.update(reservation);
			return new ResponseEntity<>(newReservation, HttpStatus.CREATED);
		} catch (ReservationNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot complete", e);
		}
	}

	@Operation(summary = "Delete the reservation.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Request has succeeded and reservation has deleted", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "Server cannot find the requested reservation to delete", content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "400", description = "server cannot process the request due to client error", content = @Content(mediaType = "application/json")) })
	@DeleteMapping("/{id}")
	public ResponseEntity deleteReservation(@PathVariable long id) {
		try {
			reservationService.delete(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (ReservationNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request cannot complete", e);
		}
	}

}
