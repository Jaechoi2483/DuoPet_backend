package com.petlogue.duopetbackend.consultation.model.service;

import com.petlogue.duopetbackend.consultation.model.dto.VetProfileDto;
import com.petlogue.duopetbackend.consultation.jpa.entity.VetProfile;
import com.petlogue.duopetbackend.consultation.jpa.repository.VetProfileRepository;
import com.petlogue.duopetbackend.user.jpa.entity.VetEntity;
import com.petlogue.duopetbackend.user.jpa.repository.VetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VetProfileService {
    
    private final VetProfileRepository vetProfileRepository;
    private final VetRepository vetRepository;
    
    /**
     * 수의사 프로필 생성
     */
    @Transactional
    public VetProfile createVetProfile(Long vetId, VetProfileDto dto) {
        // Check if vet exists
        VetEntity vet = vetRepository.findById(vetId)
                .orElseThrow(() -> new IllegalArgumentException("Vet not found with id: " + vetId));
        
        // Check if profile already exists
        if (vetProfileRepository.existsByVet_VetId(vetId)) {
            throw new IllegalStateException("Vet profile already exists for vet id: " + vetId);
        }
        
        VetProfile profile = VetProfile.builder()
                .vet(vet)
                .introduction(dto.getIntroduction())
                .consultationFee(dto.getConsultationFee() != null ? 
                        dto.getConsultationFee() : BigDecimal.valueOf(30000))
                .isAvailable(dto.getIsAvailable() != null ? 
                        dto.getIsAvailable() : "Y")
                .build();
        
        return vetProfileRepository.save(profile);
    }
    
    /**
     * 수의사 프로필 조회
     */
    public VetProfile getVetProfile(Long vetId) {
        return vetProfileRepository.findByVet_VetId(vetId)
                .orElseThrow(() -> new IllegalArgumentException("Vet profile not found for vet id: " + vetId));
    }
    
    /**
     * 수의사 프로필 업데이트
     */
    @Transactional
    public VetProfile updateVetProfile(Long vetId, VetProfileDto dto) {
        VetProfile profile = getVetProfile(vetId);
        
        if (dto.getIntroduction() != null) {
            profile.setIntroduction(dto.getIntroduction());
        }
        if (dto.getConsultationFee() != null) {
            profile.setConsultationFee(dto.getConsultationFee());
        }
        if (dto.getIsAvailable() != null) {
            profile.setIsAvailable(dto.getIsAvailable());
        }
        
        return vetProfileRepository.save(profile);
    }
    
    /**
     * 상담 가능한 수의사 목록 조회
     */
    public Page<VetProfile> getAvailableVets(Pageable pageable) {
        // isAvailable = 'Y'인 수의사만 조회
        List<VetProfile> availableVets = vetProfileRepository.findAllAvailableVets();
        
        log.info("Available vets count: {}", availableVets.size());
        if (!availableVets.isEmpty()) {
            log.info("First vet: {}", availableVets.get(0).getVet().getName());
        }
        
        // List를 Page로 변환
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), availableVets.size());
        
        List<VetProfile> pageContent = availableVets.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(
            pageContent, 
            pageable, 
            availableVets.size()
        );
    }
    
    /**
     * 온라인 상태인 수의사 목록 조회
     */
    public List<VetProfile> getOnlineVets() {
        return vetProfileRepository.findOnlineAndAvailableVets();
    }
    
    /**
     * 수의사 온라인 상태 업데이트
     */
    @Transactional
    public void updateOnlineStatus(Long vetId, boolean isOnline) {
        vetProfileRepository.updateOnlineStatus(vetId, isOnline ? "Y" : "N", java.time.LocalDateTime.now());
        log.info("Updated online status for vet {}: {}", vetId, isOnline);
    }
    
    /**
     * 수의사 상담 가능 상태 업데이트
     */
    @Transactional
    public void updateAvailability(Long vetId, boolean isAvailable) {
        vetProfileRepository.updateAvailability(vetId, isAvailable ? "Y" : "N");
        log.info("Updated availability for vet {}: {}", vetId, isAvailable);
    }
    
    /**
     * 키워드로 수의사 검색
     */
    public List<VetProfile> searchVets(String keyword) {
        return vetProfileRepository.searchByKeyword(keyword);
    }
    
    /**
     * 평점 기준으로 상위 수의사 조회
     */
    public List<VetProfile> getTopRatedVets(int limit) {
        return vetProfileRepository.findTopRatedVets(Pageable.ofSize(limit));
    }
    
    /**
     * 테스트용 - 모든 프로필 조회
     */
    public List<VetProfile> getAllProfiles() {
        return vetProfileRepository.findAll();
    }
    
    /**
     * 필터링된 수의사 목록 조회
     */
    public Page<VetProfile> getFilteredVets(Pageable pageable, String specialty, Boolean onlineOnly) {
        List<VetProfile> allVets = vetProfileRepository.findAllAvailableVets();
        
        // 필터링
        java.util.stream.Stream<VetProfile> stream = allVets.stream();
        
        if (specialty != null && !specialty.isEmpty()) {
            stream = stream.filter(vp -> vp.getVet() != null && 
                    vp.getVet().getSpecialization() != null &&
                    vp.getVet().getSpecialization().contains(specialty));
        }
        
        if (Boolean.TRUE.equals(onlineOnly)) {
            stream = stream.filter(vp -> "Y".equals(vp.getIsOnline()));
        }
        
        List<VetProfile> filteredList = stream.collect(java.util.stream.Collectors.toList());
        
        // 정렬 적용
        org.springframework.data.domain.Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            java.util.Comparator<VetProfile> comparator = null;
            for (org.springframework.data.domain.Sort.Order order : sort) {
                java.util.Comparator<VetProfile> orderComparator = getComparator(order.getProperty(), order.isAscending());
                comparator = comparator == null ? orderComparator : comparator.thenComparing(orderComparator);
            }
            if (comparator != null) {
                filteredList.sort(comparator);
            }
        }
        
        // 페이지네이션 적용
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        List<VetProfile> pageContent = start > filteredList.size() ? 
                java.util.Collections.emptyList() : filteredList.subList(start, end);
                
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filteredList.size());
    }
    
    private java.util.Comparator<VetProfile> getComparator(String property, boolean ascending) {
        java.util.Comparator<VetProfile> comparator;
        switch (property) {
            case "ratingAvg":
                comparator = java.util.Comparator.comparing(VetProfile::getRatingAvg, 
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                break;
            case "consultationCount":
                comparator = java.util.Comparator.comparing(VetProfile::getConsultationCount);
                break;
            case "consultationFee":
                comparator = java.util.Comparator.comparing(VetProfile::getConsultationFee,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                break;
            case "createdAt":
            default:
                comparator = java.util.Comparator.comparing(VetProfile::getCreatedAt,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
                break;
        }
        return ascending ? comparator : comparator.reversed();
    }
}