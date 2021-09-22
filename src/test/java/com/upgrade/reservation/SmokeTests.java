package com.upgrade.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.upgrade.reservation.model.Reservation;
import com.upgrade.reservation.resource.ReservationResource;
import com.upgrade.reservation.validator.ReservationValidator;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class SmokeTests {

	DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");

	@Autowired
	private ReservationResource resource;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	@Order(1)
	public void reservationShouldReturnDefaultAvailibility() throws Exception {
		List<LocalDate> availibility = this.restTemplate
				.getForObject("http://localhost:" + port + "/reservations/available", List.class);

		assertThat(availibility).isNotNull();
		assertThat(availibility).isNotEmpty();
	}

	@Test
	@Order(2)
	public void reservationShouldReturnAvailibility() throws Exception {
		List<LocalDate> availibility = this.restTemplate.getForObject(
				"http://localhost:" + port + "/reservations/available?startDate=2021-10-01&endDate=2021-10-30",
				List.class);

		assertThat(availibility).isNotNull();
		assertThat(availibility).isNotEmpty();
	}

	@Test
	@Order(3)
	public void createReservation() throws JSONException, ParseException {

		JSONObject reservationJsonObject = new JSONObject();
		reservationJsonObject.put("firstName", "testFistName");
		reservationJsonObject.put("lastName", "testLastName");
		reservationJsonObject.put("email", "test@email.com");
		reservationJsonObject.put("startDate", LocalDate.now().plusDays(1).toString());
		reservationJsonObject.put("endDate",
				LocalDate.now().plusDays(ReservationValidator.MAX_RESERVATION_PERIOD).toString());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<String>(reservationJsonObject.toString(), headers);
		URI location = this.restTemplate.postForLocation("http://localhost:" + port + "/reservations", request);
		assertThat(location).isNotNull();
	}

	@Test
	@Order(4)
	public void getReservation() throws JSONException, ParseException {

		Reservation reservation = this.restTemplate.getForObject("http://localhost:" + port + "/reservations/1",
				Reservation.class);

		assertThat(reservation).isNotNull();
		assertThat(reservation.getId()).isEqualTo(1);
	}

	@Test
	@Order(5)
	public void updateReservation() throws JSONException, ParseException {

		JSONObject reservationJsonObject = new JSONObject();
		reservationJsonObject.put("firstName", "newTestFistName");
		reservationJsonObject.put("lastName", "testLastName");
		reservationJsonObject.put("email", "test@email.com");
		reservationJsonObject.put("startDate", LocalDate.now().plusDays(1).toString());
		reservationJsonObject.put("endDate",
				LocalDate.now().plusDays(ReservationValidator.MAX_RESERVATION_PERIOD).toString());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> request = new HttpEntity<String>(reservationJsonObject.toString(), headers);
		this.restTemplate.put("http://localhost:" + port + "/reservations/1", request);

		Reservation reservation = this.restTemplate.getForObject("http://localhost:" + port + "/reservations/1",
				Reservation.class);

		assertThat(reservation).isNotNull();
		assertThat(reservation.getId()).isEqualTo(1);
		assertThat(reservation.getFirstName()).isEqualTo("newTestFistName");

	}

	@Test
	@Order(6)
	public void deleteReservation() throws JSONException, ParseException {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> entity = new HttpEntity<Object>(headers);
		ResponseEntity<String> response = restTemplate.exchange("http://localhost:" + port + "/reservations/1",
				HttpMethod.GET, entity, String.class);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		// delete the reservation
		this.restTemplate.delete("http://localhost:" + port + "/reservations/1");

		response = restTemplate.exchange("http://localhost:" + port + "/reservations/1", HttpMethod.GET, entity,
				String.class);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

	}

	@Test
	@Order(7)
	public void concurrenctReservationRequests() throws JSONException, InterruptedException {
		List<Object> responses = new ArrayList<>();
		int numberOfThreads = 200;
		ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);

		JSONObject reservationJsonObject = new JSONObject();
		reservationJsonObject.put("firstName", "testFistName");
		reservationJsonObject.put("lastName", "testLastName");
		reservationJsonObject.put("email", "test@email.com");
		reservationJsonObject.put("startDate", LocalDate.now().plusDays(10).toString());
		reservationJsonObject.put("endDate", LocalDate.now().plusDays(12).toString());
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> request = new HttpEntity<String>(reservationJsonObject.toString(), headers);

		headers.setContentType(MediaType.APPLICATION_JSON);

		for (int i = 0; i < numberOfThreads; i++) {
			service.submit(() -> {
				URI location = this.restTemplate.postForLocation("http://localhost:" + port + "/reservations", request);
				responses.add(location);
				latch.countDown();
			});
		}
		latch.await();
		responses.removeAll(Collections.singleton(null));
		// only one thread should be able to reserve at the same time
		responses.forEach(r -> {
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<Object> entity = new HttpEntity<Object>(headers);
			ResponseEntity<String> response = restTemplate.exchange((URI) r, HttpMethod.GET, entity, String.class);
		});
		
		assertThat(responses.size()).isEqualTo(1);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Object> entity = new HttpEntity<Object>(headers);
		ResponseEntity<String> response = restTemplate.exchange((URI) responses.get(0), HttpMethod.GET, entity,
				String.class);
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

}
