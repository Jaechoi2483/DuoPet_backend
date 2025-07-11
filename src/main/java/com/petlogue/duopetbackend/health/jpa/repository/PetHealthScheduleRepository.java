package com.petlogue.duopetbackend.health.jpa.repository;

import com.petlogue.duopetbackend.health.jpa.entity.PetHealthSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetHealthScheduleRepository extends JpaRepository<PetHealthSchedule, Long> {
    List<PetHealthSchedule> findByPet_PetId(Long petId);
}
