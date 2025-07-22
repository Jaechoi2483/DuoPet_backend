package com.petlogue.duopetbackend.health.controller;

import com.petlogue.duopetbackend.health.model.dto.PetHealthScheduleDto;
import com.petlogue.duopetbackend.health.model.dto.PetMedicalVisitDto;
import com.petlogue.duopetbackend.health.model.dto.PetVaccinDto;
import com.petlogue.duopetbackend.health.model.dto.PetWeightDto;
import com.petlogue.duopetbackend.health.model.service.HealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    // Medical Visit Endpoints
    @PostMapping("/visits")
    public ResponseEntity<Void> createMedicalVisit(@RequestBody PetMedicalVisitDto.CreateRequest dto) {
        healthService.createMedicalVisit(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/visits/{petId}")
    public ResponseEntity<List<PetMedicalVisitDto.Response>> getMedicalVisits(@PathVariable Long petId) {
        return ResponseEntity.ok(healthService.getMedicalVisits(petId));
    }

    @PutMapping("/visits/{visitId}")
    public ResponseEntity<Void> updateMedicalVisit(
            @PathVariable Long visitId,
            @RequestBody PetMedicalVisitDto.CreateRequest dto) {
        healthService.updateMedicalVisit(visitId, dto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/visits/{visitId}")
    public ResponseEntity<Void> deleteMedicalVisit(@PathVariable Long visitId) {
        healthService.deleteMedicalVisit(visitId);
        return ResponseEntity.ok().build();
    }

    // Vaccination Endpoints
    @PostMapping("/vaccinations")
    public ResponseEntity<Void> createVaccination(@RequestBody PetVaccinDto.CreateRequest dto) {
        healthService.createVaccination(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/vaccinations/{petId}")
    public ResponseEntity<List<PetVaccinDto.Response>> getVaccinations(@PathVariable Long petId) {
        return ResponseEntity.ok(healthService.getVaccinations(petId));
    }
    
    @PutMapping("/vaccinations/{vaccinationId}")
    public ResponseEntity<Void> updateVaccination(
            @PathVariable Long vaccinationId,
            @RequestBody PetVaccinDto.CreateRequest dto) {
        healthService.updateVaccination(vaccinationId, dto);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/vaccinations/{vaccinationId}")
    public ResponseEntity<Void> deleteVaccination(@PathVariable Long vaccinationId) {
        healthService.deleteVaccination(vaccinationId);
        return ResponseEntity.ok().build();
    }

    // Weight Endpoints
    @PostMapping("/weights")
    public ResponseEntity<Void> createWeight(@RequestBody PetWeightDto.CreateRequest dto) {
        healthService.createWeight(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/weights/{petId}")
    public ResponseEntity<List<PetWeightDto.Response>> getWeights(@PathVariable Long petId) {
        return ResponseEntity.ok(healthService.getWeights(petId));
    }
    
    @PutMapping("/weights/{weightId}")
    public ResponseEntity<Void> updateWeight(
            @PathVariable Long weightId,
            @RequestBody PetWeightDto.CreateRequest dto) {
        healthService.updateWeight(weightId, dto);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/weights/{weightId}")
    public ResponseEntity<Void> deleteWeight(@PathVariable Long weightId) {
        healthService.deleteWeight(weightId);
        return ResponseEntity.ok().build();
    }

    // Health Schedule Endpoints
    @PostMapping("/schedules")
    public ResponseEntity<Void> createHealthSchedule(@RequestBody PetHealthScheduleDto.CreateRequest dto) {
        healthService.createHealthSchedule(dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/schedules/{petId}")
    public ResponseEntity<List<PetHealthScheduleDto.Response>> getHealthSchedules(@PathVariable Long petId) {
        return ResponseEntity.ok(healthService.getHealthSchedules(petId));
    }
    
    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> updateHealthSchedule(
            @PathVariable Long scheduleId,
            @RequestBody PetHealthScheduleDto.CreateRequest dto) {
        healthService.updateHealthSchedule(scheduleId, dto);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteHealthSchedule(@PathVariable Long scheduleId) {
        healthService.deleteHealthSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }
}
