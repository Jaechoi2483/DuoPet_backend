package com.petlogue.duopetbackend.health.repository;

import com.petlogue.duopetbackend.health.entity.PetVaccin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetVaccinRepository extends JpaRepository<PetVaccin, Long> {
    List<PetVaccin> findByPet_PetId(Long petId);
}
