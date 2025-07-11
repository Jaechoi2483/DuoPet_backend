package com.petlogue.duopetbackend.health.service;

import com.petlogue.duopetbackend.health.dto.PetHealthScheduleDto;
import com.petlogue.duopetbackend.health.dto.PetMedicalVisitDto;
import com.petlogue.duopetbackend.health.dto.PetVaccinDto;
import com.petlogue.duopetbackend.health.dto.PetWeightDto;
import com.petlogue.duopetbackend.health.entity.PetHealthSchedule;
import com.petlogue.duopetbackend.health.entity.PetMedicalVisit;
import com.petlogue.duopetbackend.health.entity.PetVaccin;
import com.petlogue.duopetbackend.health.entity.PetWeight;
import com.petlogue.duopetbackend.health.repository.PetHealthScheduleRepository;
import com.petlogue.duopetbackend.health.repository.PetMedicalVisitRepository;
import com.petlogue.duopetbackend.health.repository.PetVaccinRepository;
import com.petlogue.duopetbackend.health.repository.PetWeightRepository;
import com.petlogue.duopetbackend.pet.entity.Pet;
import com.petlogue.duopetbackend.pet.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HealthServiceTest {

    @Mock
    private PetMedicalVisitRepository petMedicalVisitRepository;
    @Mock
    private PetVaccinRepository petVaccinRepository;
    @Mock
    private PetWeightRepository petWeightRepository;
    @Mock
    private PetHealthScheduleRepository petHealthScheduleRepository;
    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private HealthService healthService;

    private Pet testPet;

    @BeforeEach
    void setUp() {
        testPet = new Pet();
        testPet.setPetId(1L);
        testPet.setPetName("TestPet");
    }

    @Test
    void createMedicalVisit_shouldSaveVisit() {
        // Given
        PetMedicalVisitDto.CreateRequest request = new PetMedicalVisitDto.CreateRequest();
        request.setPetId(1L);
        request.setHospitalName("Test Hospital");
        request.setVisitDate(LocalDate.now());
        request.setVisitReason("Checkup");

        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petMedicalVisitRepository.save(any(PetMedicalVisit.class))).thenReturn(new PetMedicalVisit());

        // When
        healthService.createMedicalVisit(request);

        // Then
        verify(petMedicalVisitRepository, times(1)).save(any(PetMedicalVisit.class));
    }

    @Test
    void getMedicalVisits_shouldReturnVisitsForPet() {
        // Given
        PetMedicalVisit visit1 = new PetMedicalVisit();
        visit1.setVisitId(1L);
        visit1.setPet(testPet);
        visit1.setHospitalName("Hospital A");
        visit1.setVisitDate(LocalDate.now());

        PetMedicalVisit visit2 = new PetMedicalVisit();
        visit2.setVisitId(2L);
        visit2.setPet(testPet);
        visit2.setHospitalName("Hospital B");
        visit2.setVisitDate(LocalDate.now().minusDays(1));

        when(petMedicalVisitRepository.findByPet_PetId(1L)).thenReturn(Arrays.asList(visit1, visit2));

        // When
        List<PetMedicalVisitDto.Response> result = healthService.getMedicalVisits(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Hospital A", result.get(0).getHospitalName());
        assertEquals("Hospital B", result.get(1).getHospitalName());
    }

    @Test
    void createVaccination_shouldSaveVaccination() {
        // Given
        PetVaccinDto.CreateRequest request = new PetVaccinDto.CreateRequest();
        request.setPetId(1L);
        request.setVaccineName("Rabies");
        request.setScheduledDate(LocalDate.now().plusMonths(6));

        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petVaccinRepository.save(any(PetVaccin.class))).thenReturn(new PetVaccin());

        // When
        healthService.createVaccination(request);

        // Then
        verify(petVaccinRepository, times(1)).save(any(PetVaccin.class));
    }

    @Test
    void getVaccinations_shouldReturnVaccinationsForPet() {
        // Given
        PetVaccin vaccin1 = new PetVaccin();
        vaccin1.setVaccinationId(1L);
        vaccin1.setPet(testPet);
        vaccin1.setVaccineName("DHPPL");
        vaccin1.setScheduledDate(LocalDate.now());

        when(petVaccinRepository.findByPet_PetId(1L)).thenReturn(Arrays.asList(vaccin1));

        // When
        List<PetVaccinDto.Response> result = healthService.getVaccinations(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("DHPPL", result.get(0).getVaccineName());
    }

    @Test
    void createWeight_shouldSaveWeight() {
        // Given
        PetWeightDto.CreateRequest request = new PetWeightDto.CreateRequest();
        request.setPetId(1L);
        request.setWeightKg(new BigDecimal("5.5"));
        request.setMeasuredDate(LocalDate.now());

        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petWeightRepository.save(any(PetWeight.class))).thenReturn(new PetWeight());

        // When
        healthService.createWeight(request);

        // Then
        verify(petWeightRepository, times(1)).save(any(PetWeight.class));
    }

    @Test
    void getWeights_shouldReturnWeightsForPet() {
        // Given
        PetWeight weight1 = new PetWeight();
        weight1.setWeightId(1L);
        weight1.setPet(testPet);
        weight1.setWeightKg(new BigDecimal("5.0"));
        weight1.setMeasuredDate(LocalDate.now());

        when(petWeightRepository.findByPet_PetId(1L)).thenReturn(Arrays.asList(weight1));

        // When
        List<PetWeightDto.Response> result = healthService.getWeights(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("5.0"), result.get(0).getWeightKg());
    }

    @Test
    void createHealthSchedule_shouldSaveSchedule() {
        // Given
        PetHealthScheduleDto.CreateRequest request = new PetHealthScheduleDto.CreateRequest();
        request.setPetId(1L);
        request.setScheduleType("Hospital Visit");
        request.setTitle("Annual Checkup");
        request.setScheduleDate(LocalDate.now().plusMonths(3));

        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petHealthScheduleRepository.save(any(PetHealthSchedule.class))).thenReturn(new PetHealthSchedule());

        // When
        healthService.createHealthSchedule(request);

        // Then
        verify(petHealthScheduleRepository, times(1)).save(any(PetHealthSchedule.class));
    }

    @Test
    void getHealthSchedules_shouldReturnSchedulesForPet() {
        // Given
        PetHealthSchedule schedule1 = new PetHealthSchedule();
        schedule1.setScheduleId(1L);
        schedule1.setPet(testPet);
        schedule1.setScheduleType("Vaccination");
        schedule1.setTitle("Rabies Shot");
        schedule1.setScheduleDate(LocalDate.now());

        when(petHealthScheduleRepository.findByPet_PetId(1L)).thenReturn(Arrays.asList(schedule1));

        // When
        List<PetHealthScheduleDto.Response> result = healthService.getHealthSchedules(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Rabies Shot", result.get(0).getTitle());
    }
}
