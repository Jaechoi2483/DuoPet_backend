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

            log.info("공지사항 검색 실행: keyword={}, pageable={}", keyword, pageable);
            noticePage = noticeService.selectSearchTitle(keyword, pageable);
        } else {

            log.info("공지사항 전체 목록 조회 실행: pageable={}", pageable);
            noticePage = noticeService.selectList(pageable);
        }

        return ResponseEntity.ok(noticePage);
    }


    @GetMapping("/notice/{contentId}")
    public ResponseEntity<Notice> noticeDetailMethod(@PathVariable int contentId) {
        log.info("/notice/no 요청 : " + contentId);

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

        String savePath = uploadDir + "/notice";
        log.info("savePath : " + savePath);

        if (mfile != null && !mfile.isEmpty()) {

            String fileName = mfile.getOriginalFilename();
            String renameFileName = null;

            if (fileName != null && fileName.length() > 0) {
                renameFileName = FileNameChange.change(fileName, "yyyyMMddHHmmss");
                log.info("변경된 첨부 파일명 확인 : " + renameFileName);

                try {
                    mfile.transferTo(new File(savePath + "\\" + renameFileName));
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
            notice.setOriginalFilename(fileName);
            notice.setRenameFilename(renameFileName);
        }
        if (noticeService.insertNotice(notice) > 0) {
            map.put("status", "success");
            map.put("message", "새 공지 등록 성공!");
            return ResponseEntity.status(HttpStatus.CREATED).body(map);
        } else {
            map.put("status", "fail");
            map.put("message", "DB 등록 실패");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
        }
    }

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
            @ModelAttribute Notice notice,
            @RequestParam(name="deleteFlag", required=false) String deleteFlag,
            @RequestParam(name="file", required=false) MultipartFile file
    ) {
        log.info("noticeUpdateMethod : " + notice);
        notice.setContentId(contentId);

        String savePath = uploadDir + "/notice";
        log.info("savePath : " + savePath);

        if (notice.getOriginalFilename() != null && !notice.getOriginalFilename().isEmpty()
                && ((deleteFlag != null && deleteFlag.equals("yes")) || (file != null && !file.isEmpty()))) {

            new File(savePath + "/" + notice.getRenameFilename()).delete();

            notice.setOriginalFilename(null);
            notice.setRenameFilename(null);
        }

        if (file != null && !file.isEmpty()) {
            String originalFilename = file.getOriginalFilename();

            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String renameFilename = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + extension;

            try {
                file.transferTo(new File(savePath + "/" + renameFilename));

                notice.setOriginalFilename(originalFilename);
                notice.setRenameFilename(renameFilename);

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드 실패!");
            }
        }
        Notice updatedNotice = noticeService.updateNotice(notice, file);

        if(updatedNotice != null) {
            return ResponseEntity.ok("공지 수정 성공");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("공지 수정 실패!");
        }
    }


    @GetMapping("/notice/latest")
    public ResponseEntity<List<Notice>> getLatestNotices() {
        List<Notice> latestNotices = noticeService.findLatestNotices();
        return ResponseEntity.ok(latestNotices);
    }

}
