package com.petlogue.duopetbackend.pet.jpa.repository;

import com.petlogue.duopetbackend.pet.jpa.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {



}