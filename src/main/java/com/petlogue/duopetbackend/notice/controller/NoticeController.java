package com.petlogue.duopetbackend.notice.controller;

import com.petlogue.duopetbackend.notice.model.dto.Notice;
import com.petlogue.duopetbackend.notice.model.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@CrossOrigin
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notice")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> noticeListMethod(
            // @PageableDefault로 기본값(페이지당 10개, noticeNo 내림차순 정렬) 설정
            @PageableDefault(size = 10, sort = "noticeNo", direction = Sort.Direction.DESC) Pageable pageable) {

        // 1. 서비스 호출을 Page<Notice>로 받도록 변경
        Page<Notice> noticePage = noticeService.selectList(pageable);

        // 2. 응답으로 보낼 Map 생성
        Map<String, Object> response = new HashMap<>();

        // Page 객체가 content(목록)와 페이징 정보를 모두 가짐
        response.put("list", noticePage.getContent()); // 실제 공지사항 목록
        response.put("paging", noticePage);            // 전체 페이징 정보

        // 3. 목록이 있든 없든 항상 성공(200 OK)으로 응답
        // 클라이언트가 받은 paging.empty 값을 보고 "게시글 없음"을 처리하도록 하는 것이 RESTful 방식에 더 적합
        return ResponseEntity.ok(response);
    }


}
