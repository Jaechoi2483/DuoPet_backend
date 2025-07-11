package com.petlogue.duopetbackend.health.jpa.entity;

import com.petlogue.duopetbackend.pet.jpa.entity.Pet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pet_medical_visits")
@Getter
@Setter
@NoArgsConstructor
public class PetMedicalVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id")
    private Long visitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(name = "hospital_name", nullable = false, length = 100)
    private String hospitalName;

    @Column(name = "veterinarian", nullable = false, length = 100)
    private String veterinarian;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "visit_reason", nullable = false, length = 255)
    private String visitReason;

    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;

    @Column(name = "treatment", length = 1000)
    private String treatment;

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
