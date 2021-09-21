package com.upgrade.reservation.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "reservation_date")
@Table//(indexes = @Index(columnList = "date DESC", unique = true))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDateEntity {

	@Id
	private LocalDate date;
//	@ManyToOne
//    @JoinColumn(name="reservation_id", nullable=false)
//    private ReservationEntity reservation;
	

}
