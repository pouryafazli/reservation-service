package com.upgrade.reservation.repository;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.upgrade.reservation.entity.ReservationDateEntity;

@Repository
public interface ReservationDateRepository extends CrudRepository<ReservationDateEntity, LocalDate> {

	@Lock(LockModeType.PESSIMISTIC_READ)
    @Query( "select rd from reservation_date rd where date >= :startDate and date < :endDate")
	List<ReservationDateEntity> getDates(@Param("startDate") LocalDate startDate,@Param("endDate") LocalDate endDate);

}
