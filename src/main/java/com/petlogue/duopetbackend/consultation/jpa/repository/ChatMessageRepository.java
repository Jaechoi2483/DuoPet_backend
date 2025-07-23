package com.petlogue.duopetbackend.consultation.jpa.repository;

import com.petlogue.duopetbackend.consultation.jpa.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    // 상담방의 모든 메시지 조회 (페이징)
    @Query("SELECT cm FROM ChatMessage cm " +
           "JOIN FETCH cm.sender s " +
           "WHERE cm.consultationRoom.roomId = :roomId " +
           "ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByRoomId(@Param("roomId") Long roomId, Pageable pageable);
    
    // 상담방의 최근 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm " +
           "JOIN FETCH cm.sender s " +
           "WHERE cm.consultationRoom.roomId = :roomId " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findRecentMessages(@Param("roomId") Long roomId, Pageable pageable);
    
    // 읽지 않은 메시지 수 조회
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
           "WHERE cm.consultationRoom.roomId = :roomId " +
           "AND cm.sender.userId != :userId " +
           "AND cm.isRead = 'N'")
    Long countUnreadMessages(@Param("roomId") Long roomId, 
                            @Param("userId") Long userId);
    
    // 사용자별 읽지 않은 메시지 수 (전체 상담방)
    @Query("SELECT cr.roomId, COUNT(cm) " +
           "FROM ChatMessage cm " +
           "JOIN cm.consultationRoom cr " +
           "WHERE (cr.user.userId = :userId OR cr.vet.user.userId = :userId) " +
           "AND cm.sender.userId != :userId " +
           "AND cm.isRead = 'N' " +
           "GROUP BY cr.roomId")
    List<Object[]> countUnreadMessagesByUser(@Param("userId") Long userId);
    
    // 메시지 읽음 처리
    @Modifying
    @Query("UPDATE ChatMessage cm " +
           "SET cm.isRead = 'Y', " +
           "cm.readAt = :readAt " +
           "WHERE cm.consultationRoom.roomId = :roomId " +
           "AND cm.sender.userId != :readerId " +
           "AND cm.isRead = 'N'")
    int markMessagesAsRead(@Param("roomId") Long roomId,
                          @Param("readerId") Long readerId,
                          @Param("readAt") LocalDateTime readAt);
    
    // 중요 메시지만 조회
    @Query("SELECT cm FROM ChatMessage cm " +
           "JOIN FETCH cm.sender s " +
           "WHERE cm.consultationRoom.roomId = :roomId " +
           "AND cm.isImportant = 'Y' " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findImportantMessages(@Param("roomId") Long roomId);
    
    // 첨부파일이 있는 메시지만 조회
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.consultationRoom.roomId = :roomId " +
           "AND cm.fileUrl IS NOT NULL " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findMessagesWithAttachments(@Param("roomId") Long roomId);
    
    // 특정 기간의 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm " +
           "JOIN FETCH cm.sender s " +
           "WHERE cm.consultationRoom.roomId = :roomId " +
           "AND cm.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY cm.createdAt")
    List<ChatMessage> findMessagesByDateRange(@Param("roomId") Long roomId,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);
    
    // 키워드로 메시지 검색
    @Query("SELECT cm FROM ChatMessage cm " +
           "JOIN FETCH cm.sender s " +
           "WHERE cm.consultationRoom.roomId = :roomId " +
           "AND cm.content LIKE CONCAT('%', :keyword, '%') " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> searchMessages(@Param("roomId") Long roomId,
                                    @Param("keyword") String keyword);
    
    // 마지막 메시지 조회
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.consultationRoom.roomId = :roomId " +
           "ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLastMessage(@Param("roomId") Long roomId, Pageable pageable);
    
    // 시스템 메시지 생성을 위한 벌크 insert는 Service에서 처리
}