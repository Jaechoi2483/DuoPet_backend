package com.petlogue.duopetbackend.health.model.service;

import com.petlogue.duopetbackend.health.model.dto.PetHealthScheduleDto;
import com.petlogue.duopetbackend.health.model.dto.PetMedicalVisitDto;
import com.petlogue.duopetbackend.health.model.dto.PetVaccinDto;
import com.petlogue.duopetbackend.health.model.dto.PetWeightDto;
import com.petlogue.duopetbackend.health.jpa.entity.PetHealthSchedule;
import com.petlogue.duopetbackend.health.jpa.entity.PetMedicalVisit;
import com.petlogue.duopetbackend.health.jpa.entity.PetVaccin;
import com.petlogue.duopetbackend.health.jpa.entity.PetWeight;
import com.petlogue.duopetbackend.health.jpa.repository.PetHealthScheduleRepository;
import com.petlogue.duopetbackend.health.jpa.repository.PetMedicalVisitRepository;
import com.petlogue.duopetbackend.health.jpa.repository.PetVaccinRepository;
import com.petlogue.duopetbackend.health.jpa.repository.PetWeightRepository;
import com.petlogue.duopetbackend.pet.jpa.entity.PetEntity;
import com.petlogue.duopetbackend.pet.jpa.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthService {

    private final PetMedicalVisitRepository petMedicalVisitRepository;
    private final PetVaccinRepository petVaccinRepository;
    private final PetWeightRepository petWeightRepository;
    private final PetHealthScheduleRepository petHealthScheduleRepository;
    private final PetRepository petRepository; // Pet 엔티티를 가져오기 위해 추가

    // Medical Visit Service Methods
    @Transactional
    public void createMedicalVisit(PetMedicalVisitDto.CreateRequest dto) {
        PetEntity pet = petRepository.findById(dto.getPetId()).orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        PetMedicalVisit visit = new PetMedicalVisit();
        visit.setPet(pet);
        visit.setHospitalName(dto.getHospitalName());
        visit.setVeterinarian(dto.getVeterinarian());
        visit.setVisitDate(dto.getVisitDate());
        visit.setVisitReason(dto.getVisitReason());
        visit.setDiagnosis(dto.getDiagnosis());
        visit.setTreatment(dto.getTreatment());
        visit.setCost(dto.getCost());
        petMedicalVisitRepository.save(visit);
    }

    public List<PetMedicalVisitDto.Response> getMedicalVisits(Long petId) {
        return petMedicalVisitRepository.findByPet_PetId(petId).stream()
                .map(visit -> PetMedicalVisitDto.Response.builder()
                        .visitId(visit.getVisitId())
                        .hospitalName(visit.getHospitalName())
                        .veterinarian(visit.getVeterinarian())
                        .visitDate(visit.getVisitDate())
                        .visitReason(visit.getVisitReason())
                        .diagnosis(visit.getDiagnosis())
                        .treatment(visit.getTreatment())
                        .cost(visit.getCost())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateMedicalVisit(Long visitId, PetMedicalVisitDto.CreateRequest dto) {
        PetMedicalVisit visit = petMedicalVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Medical visit not found"));
        
        visit.setHospitalName(dto.getHospitalName());
        visit.setVeterinarian(dto.getVeterinarian());
        visit.setVisitDate(dto.getVisitDate());
        visit.setVisitReason(dto.getVisitReason());
        visit.setDiagnosis(dto.getDiagnosis());
        visit.setTreatment(dto.getTreatment());
        visit.setCost(dto.getCost());
        
        petMedicalVisitRepository.save(visit);
    }

    @Transactional
    public void deleteMedicalVisit(Long visitId) {
        PetMedicalVisit visit = petMedicalVisitRepository.findById(visitId)
                .orElseThrow(() -> new IllegalArgumentException("Medical visit not found"));
        petMedicalVisitRepository.delete(visit);
    }

    // Vaccination Service Methods
    @Transactional
    public void createVaccination(PetVaccinDto.CreateRequest dto) {
        PetEntity pet = petRepository.findById(dto.getPetId()).orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        PetVaccin vaccin = new PetVaccin();
        vaccin.setPet(pet);
        vaccin.setVaccineName(dto.getVaccineName());
        vaccin.setScheduledDate(dto.getScheduledDate());
        vaccin.setDescription(dto.getDescription());
        vaccin.setAdministeredDate(dto.getAdministeredDate());
        vaccin.setHospitalName(dto.getHospitalName());
        petVaccinRepository.save(vaccin);
    }

    public List<PetVaccinDto.Response> getVaccinations(Long petId) {
        return petVaccinRepository.findByPet_PetId(petId).stream()
                .map(vaccin -> PetVaccinDto.Response.builder()
                        .vaccinationId(vaccin.getVaccinationId())
                        .vaccineName(vaccin.getVaccineName())
                        .scheduledDate(vaccin.getScheduledDate())
                        .description(vaccin.getDescription())
                        .administeredDate(vaccin.getAdministeredDate())
                        .hospitalName(vaccin.getHospitalName())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void updateVaccination(Long vaccinationId, PetVaccinDto.CreateRequest dto) {
        PetVaccin vaccin = petVaccinRepository.findById(vaccinationId)
                .orElseThrow(() -> new IllegalArgumentException("Vaccination not found"));
        
        vaccin.setVaccineName(dto.getVaccineName());
        vaccin.setScheduledDate(dto.getScheduledDate());
        vaccin.setDescription(dto.getDescription());
        vaccin.setAdministeredDate(dto.getAdministeredDate());
        vaccin.setHospitalName(dto.getHospitalName());
        
        petVaccinRepository.save(vaccin);
    }
    
    @Transactional
    public void deleteVaccination(Long vaccinationId) {
        PetVaccin vaccin = petVaccinRepository.findById(vaccinationId)
                .orElseThrow(() -> new IllegalArgumentException("Vaccination not found"));
        petVaccinRepository.delete(vaccin);
    }

    // Weight Service Methods
    @Transactional
    public void createWeight(PetWeightDto.CreateRequest dto) {
        PetEntity pet = petRepository.findById(dto.getPetId()).orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        PetWeight weight = new PetWeight();
        weight.setPet(pet);
        weight.setWeightKg(dto.getWeightKg());
        weight.setMeasuredDate(dto.getMeasuredDate());
        weight.setMemo(dto.getMemo());
        petWeightRepository.save(weight);
    }

    public List<PetWeightDto.Response> getWeights(Long petId) {
        return petWeightRepository.findByPet_PetId(petId).stream()
                .map(weight -> PetWeightDto.Response.builder()
                        .weightId(weight.getWeightId())
                        .weightKg(weight.getWeightKg())
                        .measuredDate(weight.getMeasuredDate())
                        .memo(weight.getMemo())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void updateWeight(Long weightId, PetWeightDto.CreateRequest dto) {
        PetWeight weight = petWeightRepository.findById(weightId)
                .orElseThrow(() -> new IllegalArgumentException("Weight record not found"));
        
        weight.setWeightKg(dto.getWeightKg());
        weight.setMeasuredDate(dto.getMeasuredDate());
        weight.setMemo(dto.getMemo());
        
        petWeightRepository.save(weight);
    }
    
    @Transactional
    public void deleteWeight(Long weightId) {
        PetWeight weight = petWeightRepository.findById(weightId)
                .orElseThrow(() -> new IllegalArgumentException("Weight record not found"));
        petWeightRepository.delete(weight);
    }

    // Health Schedule Service Methods
    @Transactional
    public void createHealthSchedule(PetHealthScheduleDto.CreateRequest dto) {
        PetEntity pet = petRepository.findById(dto.getPetId()).orElseThrow(() -> new IllegalArgumentException("Pet not found"));
        PetHealthSchedule schedule = new PetHealthSchedule();
        schedule.setPet(pet);
        schedule.setScheduleType(dto.getScheduleType());
        schedule.setTitle(dto.getTitle());
        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setScheduleTime(dto.getScheduleTime());
        schedule.setMemo(dto.getMemo());
        petHealthScheduleRepository.save(schedule);
    }

    public List<PetHealthScheduleDto.Response> getHealthSchedules(Long petId) {
        return petHealthScheduleRepository.findByPet_PetId(petId).stream()
                .map(schedule -> PetHealthScheduleDto.Response.builder()
                        .scheduleId(schedule.getScheduleId())
                        .scheduleType(schedule.getScheduleType())
                        .title(schedule.getTitle())
                        .scheduleDate(schedule.getScheduleDate())
                        .scheduleTime(schedule.getScheduleTime())
                        .memo(schedule.getMemo())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void updateHealthSchedule(Long scheduleId, PetHealthScheduleDto.CreateRequest dto) {
        PetHealthSchedule schedule = petHealthScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Health schedule not found"));
        
        schedule.setScheduleType(dto.getScheduleType());
        schedule.setTitle(dto.getTitle());
        schedule.setScheduleDate(dto.getScheduleDate());
        schedule.setScheduleTime(dto.getScheduleTime());
        schedule.setMemo(dto.getMemo());
        
        petHealthScheduleRepository.save(schedule);
    }
    
    @Transactional
    public void deleteHealthSchedule(Long scheduleId) {
        PetHealthSchedule schedule = petHealthScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Health schedule not found"));
        petHealthScheduleRepository.delete(schedule);
    }
}
