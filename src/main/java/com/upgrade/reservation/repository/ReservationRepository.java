package com.upgrade.reservation.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.upgrade.reservation.entity.ReservationEntity;

@Repository
public interface ReservationRepository extends CrudRepository<ReservationEntity, Long> {

}
