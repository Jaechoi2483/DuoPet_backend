package com.petlogue.duopetbackend.notice.controller;

import com.petlogue.duopetbackend.common.FileNameChange;
import com.petlogue.duopetbackend.notice.model.dto.Notice;
import com.petlogue.duopetbackend.notice.model.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
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

    @DeleteMapping("/admin/notice/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable("id") int noticeId) {
        noticeService.deleteNotice(noticeId);
        // 성공 시 204 No Content 상태 코드 반환
        return ResponseEntity.noContent().build();
    }


}
