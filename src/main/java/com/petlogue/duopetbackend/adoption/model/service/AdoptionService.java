package com.petlogue.duopetbackend.adoption.model.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlogue.duopetbackend.adoption.model.dto.AdoptionAnimalDto;
import com.petlogue.duopetbackend.adoption.model.dto.PublicAnimalApiResponse;
import com.petlogue.duopetbackend.adoption.jpa.entity.AdoptionAnimal;
import com.petlogue.duopetbackend.adoption.jpa.repository.AdoptionAnimalRepository;
import com.petlogue.duopetbackend.info.jpa.entity.ShelterEntity;
import com.petlogue.duopetbackend.info.jpa.repository.ShelterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AdoptionService {
    
    private final AdoptionAnimalRepository adoptionAnimalRepository;
    private final ShelterRepository shelterRepository;
    private final PublicDataApiClient publicDataApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AdoptionService(AdoptionAnimalRepository adoptionAnimalRepository,
                          @Qualifier("shelterInfoRepository") ShelterRepository shelterRepository,
                          PublicDataApiClient publicDataApiClient) {
        this.adoptionAnimalRepository = adoptionAnimalRepository;
        this.shelterRepository = shelterRepository;
        this.publicDataApiClient = publicDataApiClient;
    }
    
    /**
     * 메인 화면용 랜덤 동물 조회
     */
    @Transactional(readOnly = true)
    public List<AdoptionAnimalDto> getRandomAnimalsForMain(int count) {
        List<AdoptionAnimal> animals = adoptionAnimalRepository.findRandomAnimals(count);
        return animals.stream()
                .map(AdoptionAnimalDto::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 보호 동물 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<AdoptionAnimalDto> getAvailableAnimals(Pageable pageable) {
        return adoptionAnimalRepository.findAvailableAnimals(pageable)
                .map(AdoptionAnimalDto::from);
    }
    
    /**
     * 동물 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public AdoptionAnimalDto getAnimalDetail(Long animalId) {
        AdoptionAnimal animal = adoptionAnimalRepository.findById(animalId)
                .orElseThrow(() -> new RuntimeException("Animal not found"));
        return AdoptionAnimalDto.from(animal);
    }
    
    /**
     * 복합 검색
     */
    @Transactional(readOnly = true)
    public Page<AdoptionAnimalDto> searchAnimals(String region, String type, 
                                                String gender, String neutered, 
                                                Pageable pageable) {
        return adoptionAnimalRepository.searchAnimals(region, type, gender, neutered, pageable)
                .map(AdoptionAnimalDto::from);
    }
    
    /**
     * 공공 API 데이터 동기화 (매일 새벽 2시 실행)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void syncAdoptionData() {
        log.info("Starting adoption data synchronization...");
        
        try {
            // API 수정일(2025-05-30) 이전 데이터 조회
            LocalDate endDate = LocalDate.of(2025, 5, 30);  // API 수정일
            LocalDate startDate = endDate.minusDays(30);    // 30일 전부터
            String bgnde = startDate.format(DateTimeFormatter.BASIC_ISO_DATE);
            String endde = endDate.format(DateTimeFormatter.BASIC_ISO_DATE);
            
            int pageNo = 1;
            int numOfRows = 100;
            boolean hasMore = true;
            
            while (hasMore) {
                Map<String, Object> response = publicDataApiClient.getAbandonmentAnimals(
                        bgnde, endde, null, "protect", pageNo, numOfRows);
                
                if (response != null && response.containsKey("response")) {
                    Map<String, Object> responseBody = (Map<String, Object>) 
                            ((Map<String, Object>) response.get("response")).get("body");
                    
                    if (responseBody != null && responseBody.containsKey("items")) {
                        Map<String, Object> items = (Map<String, Object>) responseBody.get("items");
                        List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");
                        
                        if (itemList != null && !itemList.isEmpty()) {
                            for (Map<String, Object> item : itemList) {
                                syncAnimalData(item);
                            }
                            
                            // 다음 페이지 확인
                            Integer totalCount = (Integer) responseBody.get("totalCount");
                            if (pageNo * numOfRows >= totalCount) {
                                hasMore = false;
                            } else {
                                pageNo++;
                            }
                        } else {
                            hasMore = false;
                        }
                    } else {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            }
            
            log.info("Adoption data synchronization completed.");
            
        } catch (Exception e) {
            log.error("Error during adoption data synchronization", e);
        }
    }
    
    /**
     * 개별 동물 데이터 동기화
     */
    private void syncAnimalData(Map<String, Object> itemData) {
        try {
            // Map을 PublicAnimalApiResponse.Item으로 변환
            PublicAnimalApiResponse.Item item = objectMapper.convertValue(
                    itemData, PublicAnimalApiResponse.Item.class);
            
            // DTO로 변환
            AdoptionAnimalDto dto = PublicAnimalApiResponse.toDto(item);
            
            // 기존 데이터 확인
            Optional<AdoptionAnimal> existingAnimal = adoptionAnimalRepository
                    .findByDesertionNo(item.getDesertionNo());
            
            AdoptionAnimal animal;
            if (existingAnimal.isPresent()) {
                // 업데이트
                animal = existingAnimal.get();
                updateAnimalFromDto(animal, dto);
            } else {
                // 신규 생성
                animal = createAnimalFromDto(dto);
                
                // 보호소 정보 매칭 (careRegNo가 있다면)
                // TODO: 보호소 매칭 로직 구현 필요
                // 현재는 보호소 없이 진행
                /*
                if (dto.getShelterName() != null) {
                    Optional<ShelterEntity> shelter = shelterRepository
                            .findByShelterName(dto.getShelterName());
                    shelter.ifPresent(animal::setShelter);
                }
                */
            }
            
            adoptionAnimalRepository.save(animal);
            
        } catch (Exception e) {
            log.error("Error syncing animal data: " + itemData, e);
        }
    }
    
    /**
     * DTO에서 엔티티 생성
     */
    private AdoptionAnimal createAnimalFromDto(AdoptionAnimalDto dto) {
        return AdoptionAnimal.builder()
                .desertionNo(dto.getDesertionNo())
                .animalType(dto.getAnimalType())
                .breed(dto.getBreed())
                .age(dto.getAge())
                .gender(dto.getGender())
                .neutered(dto.getNeutered())
                .happenDate(dto.getHappenDate())
                .happenPlace(dto.getHappenPlace())
                .specialMark(dto.getSpecialMark())
                .publicNoticeNo(dto.getPublicNoticeNo())
                .publicNoticeStart(dto.getPublicNoticeStart())
                .publicNoticeEnd(dto.getPublicNoticeEnd())
                .imageUrl(dto.getImageUrl())
                .weight(dto.getWeight())
                .colorCd(dto.getColorCd())
                .processState(dto.getProcessState())
                .status("AVAILABLE")
                .apiSource("PUBLIC_API")
                .build();
    }
    
    /**
     * DTO로 엔티티 업데이트
     */
    private void updateAnimalFromDto(AdoptionAnimal animal, AdoptionAnimalDto dto) {
        animal.setAnimalType(dto.getAnimalType());
        animal.setBreed(dto.getBreed());
        animal.setAge(dto.getAge());
        animal.setGender(dto.getGender());
        animal.setNeutered(dto.getNeutered());
        animal.setHappenDate(dto.getHappenDate());
        animal.setHappenPlace(dto.getHappenPlace());
        animal.setSpecialMark(dto.getSpecialMark());
        animal.setPublicNoticeNo(dto.getPublicNoticeNo());
        animal.setPublicNoticeStart(dto.getPublicNoticeStart());
        animal.setPublicNoticeEnd(dto.getPublicNoticeEnd());
        animal.setImageUrl(dto.getImageUrl());
        animal.setWeight(dto.getWeight());
        animal.setColorCd(dto.getColorCd());
        animal.setProcessState(dto.getProcessState());
    }
}