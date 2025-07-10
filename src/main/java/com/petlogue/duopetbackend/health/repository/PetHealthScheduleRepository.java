package com.petlogue.duopetbackend.health.repository;

import com.petlogue.duopetbackend.health.entity.PetHealthSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetHealthScheduleRepository extends JpaRepository<PetHealthSchedule, Long> {
    List<PetHealthSchedule> findByPet_PetId(Long petId);
}
