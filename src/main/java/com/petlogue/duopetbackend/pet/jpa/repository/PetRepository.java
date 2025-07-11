package com.petlogue.duopetbackend.pet.repository;

import com.petlogue.duopetbackend.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {



}