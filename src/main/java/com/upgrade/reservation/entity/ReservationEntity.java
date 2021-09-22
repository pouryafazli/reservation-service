package com.upgrade.reservation.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "reservation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {
	
	  @Id
	  @GeneratedValue
	  private long id;

	  private String email;

	  private String firstName;
	  
	  private String lastName;

	  private LocalDate startDate;
	  
	  private LocalDate endDate;
}
