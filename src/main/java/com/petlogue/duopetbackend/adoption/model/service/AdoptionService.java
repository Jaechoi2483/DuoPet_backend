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
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;

@Slf4j
@Service
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
    public void scheduledSyncAdoptionData() {
        syncAdoptionData();
    }
    
    /**
     * 공공 API 데이터 동기화 (수동 실행용)
     */
    public Map<String, Object> syncAdoptionData() {
        log.info("Starting adoption data synchronization...");
        
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        int totalProcessed = 0;
        
        try {
            int pageNo = 1;
            int numOfRows = 1000;  // 페이지당 1000개 (최대값)
            
            // 지역 코드 없이 전국 데이터 조회
            String uprCd = null;
            
            log.info("Starting animal data synchronization with pageNo={}, numOfRows={}, uprCd={}", pageNo, numOfRows, uprCd);
            
            // 1페이지만 호출 (페이지네이션 없음)
            Map<String, Object> response = publicDataApiClient.getAbandonmentAnimals(
                    null, null, null, null, pageNo, numOfRows, uprCd);
            
            if (response != null && response.containsKey("response")) {
                Map<String, Object> responseBody = (Map<String, Object>) 
                        ((Map<String, Object>) response.get("response")).get("body");
                
                if (responseBody != null && responseBody.containsKey("items")) {
                    Map<String, Object> items = (Map<String, Object>) responseBody.get("items");
                    List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");
                    
                    if (itemList != null && !itemList.isEmpty()) {
                        log.info("Processing {} animals from API response", itemList.size());
                        for (Map<String, Object> item : itemList) {
                            totalProcessed++;
                            try {
                                syncAnimalData(item);
                                successCount++;
                            } catch (Exception e) {
                                failureCount++;
                                String desertionNo = item.get("desertionNo") != null ? item.get("desertionNo").toString() : "unknown";
                                log.error("Failed to sync animal: desertionNo={}, error={}", desertionNo, e.getMessage(), e);
                            }
                        }
                        
                        // 전체 건수 로그
                        Integer totalCount = (Integer) responseBody.get("totalCount");
                        log.info("Total available animals in API: {}, Processed: {}", totalCount, totalProcessed);
                    } else {
                        log.warn("No items found in API response");
                    }
                } else {
                    log.error("Invalid response structure: no items found");
                }
            } else {
                log.error("Failed to get response from API");
            }
            
            result.put("totalProcessed", totalProcessed);
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            
            log.info("Adoption data synchronization completed. Total: {}, Success: {}, Failed: {}", 
                    totalProcessed, successCount, failureCount);
            
        } catch (Exception e) {
            log.error("Error during adoption data synchronization", e);
            result.put("totalProcessed", totalProcessed);
            result.put("successCount", successCount);
            result.put("failureCount", failureCount);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 개별 동물 데이터 동기화
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncAnimalData(Map<String, Object> itemData) {
        try {
            // Map을 PublicAnimalApiResponse.Item으로 변환
            PublicAnimalApiResponse.Item item = objectMapper.convertValue(
                    itemData, PublicAnimalApiResponse.Item.class);
            
            // DTO로 변환
            AdoptionAnimalDto dto = PublicAnimalApiResponse.toDto(item);
            
            // 나이 파싱 문제 로깅 (개선된 로깅)
            if (item.getAge() != null) {
                if (dto.getAge() == null) {
                    log.warn("Age parsing failed for desertionNo: {}, original age: '{}', format not recognized", 
                            item.getDesertionNo(), item.getAge());
                } else if (dto.getAge() < 0 || dto.getAge() > 35) {
                    log.warn("Suspicious age value for desertionNo: {}, parsed age: {}, original: '{}', age out of valid range", 
                            item.getDesertionNo(), dto.getAge(), item.getAge());
                } else {
                    log.debug("Age parsed successfully for desertionNo: {}, age: {}, original: '{}'", 
                            item.getDesertionNo(), dto.getAge(), item.getAge());
                }
            }
            
            // 중복 체크 없이 항상 새로운 엔티티 생성 (임시 해결책)
            AdoptionAnimal animal = createAnimalFromDto(dto);
            
            // 중복 체크 및 업데이트
            Optional<AdoptionAnimal> existingAnimal = adoptionAnimalRepository.findByDesertionNo(item.getDesertionNo());
            if (existingAnimal.isPresent()) {
                log.debug("Animal with desertionNo {} already exists, updating", item.getDesertionNo());
                // 기존 동물 정보 업데이트
                updateAnimalFromDto(existingAnimal.get(), dto);
                adoptionAnimalRepository.save(existingAnimal.get());
                return;
            }
            
            // 공공 API 보호소 정보 저장 (FK 매핑은 추후 진행)
            if (dto.getShelterName() != null) {
                animal.setApiShelterName(dto.getShelterName());
                animal.setApiShelterTel(dto.getShelterPhone());
                animal.setApiShelterAddr(dto.getShelterAddress());
            }
            
            adoptionAnimalRepository.save(animal);
            
        } catch (Exception e) {
            log.error("Error syncing animal data: " + itemData, e);
            // 독립적인 트랜잭션이므로 예외를 다시 던져서 해당 트랜잭션만 롤백
            throw new RuntimeException("Sync failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * DTO에서 엔티티 생성
     */
    private AdoptionAnimal createAnimalFromDto(AdoptionAnimalDto dto) {
        // 이름 생성 - 품종이나 유기번호 사용
        String name = dto.getBreed() != null ? dto.getBreed() : "보호동물";
        if (dto.getDesertionNo() != null) {
            name = name + "-" + dto.getDesertionNo().substring(dto.getDesertionNo().length() - 4);
        }
        
        return AdoptionAnimal.builder()
                .desertionNo(dto.getDesertionNo())
                .name(name)  // NAME 필드 추가
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
                .intakeDate(dto.getHappenDate())  // 발견일을 입소일로 사용
                .apiSource("PUBLIC_API")
                .status("AVAILABLE")  // 기본값 설정
                .apiShelterName(dto.getShelterName())
                .apiShelterTel(dto.getShelterPhone())
                .apiShelterAddr(dto.getShelterAddress())
                .apiOrgNm(dto.getOrgNm())
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
        
        // intake_date가 없으면 happen_date로 설정
        if (animal.getIntakeDate() == null && dto.getHappenDate() != null) {
            animal.setIntakeDate(dto.getHappenDate());
        }
        
        // 보호소 정보 업데이트
        if (dto.getShelterName() != null) {
            animal.setApiShelterName(dto.getShelterName());
            animal.setApiShelterTel(dto.getShelterPhone());
            animal.setApiShelterAddr(dto.getShelterAddress());
            animal.setApiOrgNm(dto.getOrgNm());
        }
    }
    
    /**
     * 디버깅용 동기화 테스트
     */
    public Map<String, Object> debugSyncTest() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // API 호출 테스트
            String uprCd = "6410000";
            
            // 먼저 API URL을 확인하기 위해 로그 추가
            log.info("Calling debugSyncTest with uprCd: {}", uprCd);
            
            // URI를 확인하기 위해 별도로 호출
            result.put("uprCd", uprCd);
            result.put("numOfRows", 10);
            result.put("pageNo", 1);
            
            Map<String, Object> response = null;
            try {
                response = publicDataApiClient.getAbandonmentAnimals(
                        null, null, null, null, 1, 10, uprCd);
                
                result.put("apiResponse", response);
                result.put("success", true);
            } catch (Exception apiEx) {
                result.put("apiError", apiEx.getMessage());
                result.put("apiErrorType", apiEx.getClass().getName());
                result.put("apiErrorDetail", apiEx.getCause() != null ? apiEx.getCause().getMessage() : "No cause");
                result.put("success", false);
                
                // 대체 방법으로 직접 호출 테스트
                return result;
            }
            
            if (response != null && response.containsKey("response")) {
                Map<String, Object> resp = (Map<String, Object>) response.get("response");
                Map<String, Object> body = (Map<String, Object>) resp.get("body");
                
                result.put("totalCount", body.get("totalCount"));
                result.put("hasItems", body.containsKey("items"));
                
                if (body.containsKey("items")) {
                    Map<String, Object> items = (Map<String, Object>) body.get("items");
                    if (items.containsKey("item")) {
                        List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");
                        result.put("itemCount", itemList.size());
                        if (!itemList.isEmpty()) {
                            result.put("firstItem", itemList.get(0));
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getName());
        }
        
        return result;
    }
    
    /**
     * 디버깅용 설정 확인
     */
    public Map<String, Object> getDebugConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // PublicDataApiClient에서 설정 가져오기
        config.put("serviceKeyLength", publicDataApiClient.getServiceKeyLength());
        config.put("baseUrl", publicDataApiClient.getBaseUrl());
        
        return config;
    }
    
    /**
     * 단일 동물 동기화 테스트
     */
    public Map<String, Object> testSyncOne() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 다른 페이지의 데이터를 가져오기 위해 pageNo를 2로 설정
            Map<String, Object> response = publicDataApiClient.getAbandonmentAnimals(
                    null, null, null, null, 2, 10, "6410000");
                    
            if (response != null && response.containsKey("response")) {
                Map<String, Object> resp = (Map<String, Object>) response.get("response");
                Map<String, Object> body = (Map<String, Object>) resp.get("body");
                
                // 전체 개수 확인
                result.put("totalCount", body.get("totalCount"));
                result.put("pageNo", body.get("pageNo"));
                result.put("numOfRows", body.get("numOfRows"));
                
                Map<String, Object> items = (Map<String, Object>) body.get("items");
                List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");
                
                result.put("itemCount", itemList != null ? itemList.size() : 0);
                
                if (itemList != null && !itemList.isEmpty()) {
                    // 첫 번째와 두 번째 아이템의 desertionNo 확인
                    Map<String, Object> firstItem = itemList.get(0);
                    result.put("firstDesertionNo", firstItem.get("desertionNo"));
                    
                    if (itemList.size() > 1) {
                        Map<String, Object> secondItem = itemList.get(1);
                        result.put("secondDesertionNo", secondItem.get("desertionNo"));
                    }
                    
                    // 모든 desertionNo 리스트
                    List<String> allDesertionNos = itemList.stream()
                            .map(item -> (String) item.get("desertionNo"))
                            .collect(Collectors.toList());
                    result.put("allDesertionNos", allDesertionNos);
                }
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 직접 API 테스트 (PublicDataApiClient 사용)
     */
    public String directApiTest() {
        try {
            // PublicDataApiClient를 통해 테스트
            Map<String, Object> response = publicDataApiClient.getAbandonmentAnimals(
                    null, null, null, null, 1, 1, "6410000");
            
            // Map을 JSON 문자열로 변환
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * 데이터베이스 현황 확인
     */
    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 전체 동물 수
        long totalCount = adoptionAnimalRepository.count();
        status.put("totalCount", totalCount);
        
        // 상태별 동물 수
        List<AdoptionAnimal> allAnimals = adoptionAnimalRepository.findAll();
        Map<String, Long> statusCount = allAnimals.stream()
                .collect(Collectors.groupingBy(
                        animal -> animal.getProcessState() != null ? animal.getProcessState() : "null",
                        Collectors.counting()
                ));
        status.put("statusCount", statusCount);
        
        // 최근 저장된 동물 5개
        List<Map<String, Object>> recentAnimals = adoptionAnimalRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(animal -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("animalId", animal.getAnimalId());
                    info.put("desertionNo", animal.getDesertionNo());
                    info.put("breed", animal.getBreed());
                    info.put("createdAt", animal.getCreatedAt());
                    return info;
                })
                .collect(Collectors.toList());
        status.put("recentAnimals", recentAnimals);
        
        return status;
    }
    
    /**
     * API 페이지네이션 테스트
     */
    public Map<String, Object> testPagination() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 페이지 1 테스트
            Map<String, Object> page1 = publicDataApiClient.getAbandonmentAnimals(
                    null, null, null, null, 1, 5, "6410000");
            
            if (page1 != null && page1.containsKey("response")) {
                Map<String, Object> body1 = (Map<String, Object>) 
                        ((Map<String, Object>) page1.get("response")).get("body");
                result.put("page1_totalCount", body1.get("totalCount"));
                
                Map<String, Object> items1 = (Map<String, Object>) body1.get("items");
                List<Map<String, Object>> itemList1 = (List<Map<String, Object>>) items1.get("item");
                
                if (itemList1 != null && !itemList1.isEmpty()) {
                    List<String> page1Nos = itemList1.stream()
                            .map(item -> (String) item.get("desertionNo"))
                            .collect(Collectors.toList());
                    result.put("page1_desertionNos", page1Nos);
                }
            }
            
            // 페이지 2 테스트
            Map<String, Object> page2 = publicDataApiClient.getAbandonmentAnimals(
                    null, null, null, null, 2, 5, "6410000");
                    
            if (page2 != null && page2.containsKey("response")) {
                Map<String, Object> body2 = (Map<String, Object>) 
                        ((Map<String, Object>) page2.get("response")).get("body");
                
                Map<String, Object> items2 = (Map<String, Object>) body2.get("items");
                List<Map<String, Object>> itemList2 = (List<Map<String, Object>>) items2.get("item");
                
                if (itemList2 != null && !itemList2.isEmpty()) {
                    List<String> page2Nos = itemList2.stream()
                            .map(item -> (String) item.get("desertionNo"))
                            .collect(Collectors.toList());
                    result.put("page2_desertionNos", page2Nos);
                }
            }
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 잘못된 나이 데이터 확인 및 정리
     * @return 정리 결과
     */
    public Map<String, Object> cleanupInvalidAgeData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 비정상적인 나이 값을 가진 동물들 조회
            List<AdoptionAnimal> problematicAnimals = adoptionAnimalRepository.findAll().stream()
                    .filter(animal -> animal.getAge() != null && (animal.getAge() < 0 || animal.getAge() > 30))
                    .collect(Collectors.toList());
            
            result.put("totalProblematicAnimals", problematicAnimals.size());
            
            // 문제가 있는 데이터 상세 정보
            List<Map<String, Object>> details = problematicAnimals.stream()
                    .map(animal -> {
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("animalId", animal.getAnimalId());
                        detail.put("desertionNo", animal.getDesertionNo());
                        detail.put("age", animal.getAge());
                        detail.put("name", animal.getName());
                        detail.put("breed", animal.getBreed());
                        return detail;
                    })
                    .collect(Collectors.toList());
            
            result.put("problematicData", details);
            
            // 비정상적인 나이 값을 null로 설정
            int updatedCount = 0;
            for (AdoptionAnimal animal : problematicAnimals) {
                animal.setAge(null);
                adoptionAnimalRepository.save(animal);
                updatedCount++;
            }
            
            result.put("updatedCount", updatedCount);
            result.put("success", true);
            
            log.info("Cleaned up {} animals with invalid age data", updatedCount);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("success", false);
            log.error("Error during age data cleanup", e);
        }
        
        return result;
    }
}