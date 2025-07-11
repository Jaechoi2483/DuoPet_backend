package com.petlogue.duopetbackend.health.jpa.repository;

import com.petlogue.duopetbackend.health.jpa.entity.PetMedicalVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetMedicalVisitRepository extends JpaRepository<PetMedicalVisit, Long> {
    List<PetMedicalVisit> findByPet_PetId(Long petId);
}
