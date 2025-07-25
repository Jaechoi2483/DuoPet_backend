package com.petlogue.duopetbackend.notice.controller;

import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.notice.model.dto.Notice;
import com.petlogue.duopetbackend.notice.model.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
public class NoticeController {

    private final NoticeService noticeService;
    @Value("${file.upload-dir}")
    private String uploadDir;



    @GetMapping("/notice")
    public ResponseEntity<Page<Notice>> getNoticeList(
            @PageableDefault(size = 10, sort = "contentId", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword) {

        Page<Notice> noticePage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드가 있으면 검색 로직 호출
            log.info("공지사항 검색 실행: keyword={}, pageable={}", keyword, pageable);
            noticePage = noticeService.selectSearchTitle(keyword, pageable);
        } else {
            // 키워드가 없으면 전체 목록 조회 로직 호출
            log.info("공지사항 전체 목록 조회 실행: pageable={}", pageable);
            noticePage = noticeService.selectList(pageable);
        }

        return ResponseEntity.ok(noticePage);
    }


    @GetMapping("/notice/{contentId}") // path 에 함께 전송오는 값을 받아줄 변수 '/{변수명}' 표시함, 임의대로 지정함
    public ResponseEntity<Notice> noticeDetailMethod(@PathVariable int contentId) {
        log.info("/notice/no 요청 : " + contentId); //전송받은 값 확인

        Notice notice = noticeService.selectNotice(contentId);
        //조회수 1증가 처리
        noticeService.updateAddViewCount(contentId);

        return notice != null ? new ResponseEntity<>(notice, HttpStatus.OK): new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    @PostMapping(value = "/admin/notice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> noticeInsertMethod(
            @ModelAttribute Notice notice,
            @RequestParam(name="ofile", required=false) MultipartFile mfile	) {
        log.info("/admin/notice : " + notice);

        Map<String, Object> map = new HashMap<>();

        //공지사항 첨부파일 저장 폴더를 경로 저장 (application.properties 에 경로 설정 추가)
        String savePath = uploadDir + "/notice";
        log.info("savePath : " + savePath);

        //첨부파일이 있을 때
        if (mfile != null && !mfile.isEmpty()) {
            // 전송온 파일이름 추출함
            String fileName = mfile.getOriginalFilename();
            String renameFileName = null;

            //저장 폴더에는 변경된 파일이름을 파일을 저장 처리함
            //바꿀 파일명 : 년월일시분초.확장자
            if (fileName != null && fileName.length() > 0) {
                renameFileName = FileNameChange.change(fileName, "yyyyMMddHHmmss");
                log.info("변경된 첨부 파일명 확인 : " + renameFileName);

                try {
                    //저장 폴더에 바뀐 파일명으로 파일 저장하기
                    mfile.transferTo(new File(savePath + "\\" + renameFileName));
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            } //파일명 바꾸어 저장하기

            //notice 객체에 첨부파일 정보 저장하기
            notice.setOriginalFilename(fileName);
            notice.setRenameFilename(renameFileName);
        } //첨부파일 있을 때



        if (noticeService.insertNotice(notice) > 0) {
            map.put("status", "success");
            map.put("message", "새 공지 등록 성공!");
            return ResponseEntity.status(HttpStatus.CREATED).body(map);
        } else {
            map.put("status", "fail");
            map.put("message", "DB 등록 실패");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
        }

    }  // insertNotice closed

    @DeleteMapping("/notice/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable("id") int noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notice/nfdown")
    public ResponseEntity<Resource> fileDownMethod(
            @RequestParam("ofile") String originalFileName,
            @RequestParam("rfile") String renameFileName) {

        log.info("/notice/nfdown : " + originalFileName + ", " + renameFileName);

        // 1. Path 객체 생성 및 파일 경로 보안 강화
        Path uploadPath = Paths.get(uploadDir, "notice").toAbsolutePath().normalize();
        Path filePath = uploadPath.resolve(renameFileName).normalize();

        // (보안) 지정된 업로드 경로를 벗어나는지 확인
        if (!filePath.startsWith(uploadPath)) {
            log.warn("Path traversal attempt detected: {}", renameFileName);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            // 2. Resource 객체 생성 및 파일 존재 여부 확인
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.error("File not found or not readable: {}", filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 3. Content-Disposition 헤더 설정 (Spring 방식)
            String contentDisposition = ContentDisposition.builder("attachment")
                    .filename(originalFileName, StandardCharsets.UTF_8)
                    .build()
                    .toString();

            // 4. MimeType 자동 감지 (선택 사항이지만 권장)
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream"; // 알 수 없는 경우 기본값 설정
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("Malformed URL for file path: " + filePath, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (IOException e) {
            log.error("Failed to determine file type for: " + filePath, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping("/notice/{id}")
    public ResponseEntity<String> noticeUpdateMethod(
            @PathVariable("id") int contentId,
            @ModelAttribute Notice notice, // @RequestPart 대신 @ModelAttribute 사용
            @RequestParam(name="deleteFlag", required=false) String deleteFlag,
            @RequestParam(name="file", required=false) MultipartFile file // 프론트와 이름 맞춤
    ) {
        log.info("noticeUpdateMethod : " + notice);
        notice.setContentId(contentId); // URL의 ID를 notice 객체에 설정

        // 첨부파일이 저장될 폴더 경로
        String savePath = uploadDir + "/notice";
        log.info("savePath : " + savePath);

        // 1. 기존 파일 삭제 로직
        // (delFlag가 "yes"로 오거나, 새 파일이 업로드된 경우 기존 파일 삭제)
        if (notice.getOriginalFilename() != null && !notice.getOriginalFilename().isEmpty()
                && ((deleteFlag != null && deleteFlag.equals("yes")) || (file != null && !file.isEmpty()))) {

            // 실제 저장된 파일(renameFilename)을 폴더에서 삭제
            new File(savePath + "/" + notice.getRenameFilename()).delete();

            // notice 객체 안의 파일 정보도 null로 초기화
            notice.setOriginalFilename(null);
            notice.setRenameFilename(null);
        }

        // 2. 새 파일 업로드 로직
        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();

            // 파일 이름 변경 로직 (제공해주신 FileNameChange.change 와 유사하게 구현)
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String renameFilename = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + extension;

            try {
                // 파일을 실제 경로에 저장
                file.transferTo(new File(savePath + "/" + renameFilename));

                // notice 객체에 새 파일 정보 저장
                notice.setOriginalFilename(originalFilename);
                notice.setRenameFilename(renameFilename);

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패!");
            }
        }
        Notice updatedNotice = noticeService.updateNotice(notice, file);

        // 3. 서비스 호출하여 DB 업데이트
        // 이 구조에서는 서비스가 파일 자체를 알 필요 없이, notice 객체 안의 정보만 DB에 저장합니다.
        if(updatedNotice != null) { // 반환된 객체가 null이 아니면 성공
            return ResponseEntity.ok("공지 수정 성공");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("공지 수정 실패!");
        }
    }
    @GetMapping("/notice/latest")
    public ResponseEntity<List<Notice>> getLatestNotices() { // ✅ 반환 타입을 List<Notice>로 변경
        List<Notice> latestNotices = noticeService.findLatestNotices();
        return ResponseEntity.ok(latestNotices);
    }

}
