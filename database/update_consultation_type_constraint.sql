-- CONSULTATION_ROOM 테이블의 consultation_type 제약조건에 QNA 추가
-- 실행일: 2025-07-28
-- 목적: Q&A 상담 기능 추가를 위한 제약조건 수정

-- 1. 기존 제약조건 확인
SELECT constraint_name, constraint_type, search_condition, status
FROM user_constraints
WHERE table_name = 'CONSULTATION_ROOM'
AND constraint_name = 'CK_CR_TYPE';

-- 2. 기존 제약조건 삭제
ALTER TABLE CONSULTATION_ROOM DROP CONSTRAINT CK_CR_TYPE;

-- 3. QNA를 포함한 새로운 제약조건 추가
ALTER TABLE CONSULTATION_ROOM 
ADD CONSTRAINT CK_CR_TYPE 
CHECK (consultation_type IN ('CHAT', 'VIDEO', 'PHONE', 'QNA'));

-- 4. 제약조건이 올바르게 추가되었는지 확인
SELECT constraint_name, constraint_type, search_condition, status
FROM user_constraints
WHERE table_name = 'CONSULTATION_ROOM'
AND constraint_name = 'CK_CR_TYPE';

-- 5. 현재 consultation_type 값들과 개수 확인 (옵션)
SELECT DISTINCT consultation_type, COUNT(*) as count
FROM CONSULTATION_ROOM
GROUP BY consultation_type
ORDER BY consultation_type;

-- 6. 변경사항 커밋
COMMIT;

-- 실행 결과 확인 쿼리
-- QNA 타입으로 테스트 데이터 삽입이 가능한지 확인 (나중에 롤백할 것)
/*
-- 테스트용 (실행하지 마세요 - 참고용)
INSERT INTO CONSULTATION_ROOM (
    room_uuid, user_id, vet_id, room_status, consultation_type
) VALUES (
    SYS_GUID(), 1, 1, 'CREATED', 'QNA'
);
ROLLBACK;
*/