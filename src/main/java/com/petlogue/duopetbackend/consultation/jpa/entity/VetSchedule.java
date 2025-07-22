package com.petlogue.duopetbackend.consultation.jpa.entity;

import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "vet_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"vet"})
public class VetSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vet_id", nullable = false)
    private VetEntity vet;
    
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;
    
    @Column(name = "start_time", length = 5, nullable = false)
    private String startTime; // HH:MI format
    
    @Column(name = "end_time", length = 5, nullable = false)
    private String endTime; // HH:MI format
    
    @Column(name = "max_consultations")
    @Builder.Default
    private Integer maxConsultations = 10;
    
    @Column(name = "current_bookings")
    @Builder.Default
    private Integer currentBookings = 0;
    
    @Column(name = "is_available", length = 1, nullable = false)
    @Builder.Default
    private String isAvailable = "Y";
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 편의 메서드
    public boolean isAvailable() {
        return "Y".equals(this.isAvailable) && 
               this.currentBookings < this.maxConsultations;
    }
    
    public boolean canBook() {
        return isAvailable() && 
               LocalDate.now().isBefore(this.scheduleDate);
    }
    
    public void incrementBooking() {
        if (this.currentBookings < this.maxConsultations) {
            this.currentBookings++;
            if (this.currentBookings >= this.maxConsultations) {
                this.isAvailable = "N";
            }
        }
    }
    
    public void decrementBooking() {
        if (this.currentBookings > 0) {
            this.currentBookings--;
            if (this.currentBookings < this.maxConsultations) {
                this.isAvailable = "Y";
            }
        }
    }
    
    public LocalTime getStartTimeAsLocalTime() {
        String[] parts = this.startTime.split(":");
        return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
    
    public LocalTime getEndTimeAsLocalTime() {
        String[] parts = this.endTime.split(":");
        return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
    
    public LocalDateTime getScheduledStartDateTime() {
        return LocalDateTime.of(this.scheduleDate, getStartTimeAsLocalTime());
    }
    
    public LocalDateTime getScheduledEndDateTime() {
        return LocalDateTime.of(this.scheduleDate, getEndTimeAsLocalTime());
    }
}