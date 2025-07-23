package com.petlogue.duopetbackend.consultation.controller;

import com.petlogue.duopetbackend.consultation.model.dto.ApiResponse;
import com.petlogue.duopetbackend.consultation.model.dto.ChatMessageDto;
import com.petlogue.duopetbackend.consultation.model.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/consultation/messages")
@RequiredArgsConstructor
public class ChatMessageController {
    
    private final ChatMessageService chatMessageService;
    
    /**
     * 상담방 메시지 목록 조회 (페이징)
     * GET /api/consultation/messages/room/{roomId}
     */
    @GetMapping("/room/{roomId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<ChatMessageDto>>> getRoomMessages(
            @PathVariable Long roomId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // TODO: Verify user has access to this room
        
        Page<ChatMessageDto> messages = chatMessageService.getRoomMessages(roomId, pageable);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
    
    /**
     * 최근 메시지 조회
     * GET /api/consultation/messages/room/{roomId}/recent
     */
    @GetMapping("/room/{roomId}/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getRecentMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "20") int limit) {
        
        List<ChatMessageDto> messages = chatMessageService.getRecentMessages(roomId, limit);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
    
    /**
     * 읽지 않은 메시지 수 조회
     * GET /api/consultation/messages/room/{roomId}/unread-count
     */
    @GetMapping("/room/{roomId}/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // TODO: Get userId from authenticated user
        Long userId = 1L; // Placeholder
        
        Long unreadCount = chatMessageService.getUnreadMessageCount(roomId, userId);
        Map<String, Long> result = Map.of("unreadCount", unreadCount);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 메시지 읽음 처리
     * POST /api/consultation/messages/room/{roomId}/mark-read
     */
    @PostMapping("/room/{roomId}/mark-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // TODO: Get userId from authenticated user
        Long userId = 1L; // Placeholder
        
        chatMessageService.markMessagesAsRead(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("메시지를 읽음 처리했습니다.", null));
    }
    
    /**
     * 중요 메시지 토글
     * POST /api/consultation/messages/{messageId}/toggle-important
     */
    @PostMapping("/{messageId}/toggle-important")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> toggleImportant(
            @PathVariable Long messageId) {
        
        try {
            chatMessageService.toggleImportantMessage(messageId);
            return ResponseEntity.ok(ApiResponse.success("중요 메시지 상태가 변경되었습니다.", null));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 메시지 검색
     * GET /api/consultation/messages/room/{roomId}/search
     */
    @GetMapping("/room/{roomId}/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> searchMessages(
            @PathVariable Long roomId,
            @RequestParam String keyword) {
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("검색어를 입력해주세요."));
        }
        
        List<ChatMessageDto> results = chatMessageService.searchMessages(roomId, keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
    
    /**
     * 첨부파일이 있는 메시지 조회
     * GET /api/consultation/messages/room/{roomId}/attachments
     */
    @GetMapping("/room/{roomId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getMessagesWithAttachments(
            @PathVariable Long roomId) {
        
        List<ChatMessageDto> messages = chatMessageService.getMessagesWithAttachments(roomId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
    
    /**
     * 마지막 메시지 조회
     * GET /api/consultation/messages/room/{roomId}/last
     */
    @GetMapping("/room/{roomId}/last")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatMessageDto>> getLastMessage(
            @PathVariable Long roomId) {
        
        ChatMessageDto lastMessage = chatMessageService.getLastMessage(roomId);
        if (lastMessage == null) {
            return ResponseEntity.ok(ApiResponse.success("메시지가 없습니다.", null));
        }
        
        return ResponseEntity.ok(ApiResponse.success(lastMessage));
    }
}