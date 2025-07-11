package com.petlogue.duopetbackend.health.jpa.repository;

import com.petlogue.duopetbackend.health.jpa.entity.PetWeight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetWeightRepository extends JpaRepository<PetWeight, Long> {
    List<PetWeight> findByPet_PetId(Long petId);
}
