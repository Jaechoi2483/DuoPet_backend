-- 공공 API 데이터 테스트 삽입
INSERT INTO DUOPET.SHELTER_ANIMALS (
    NAME,
    ANIMAL_TYPE,
    BREED,
    AGE,
    GENDER,
    NEUTERED,
    STATUS,
    HAPPEN_DATE,
    HAPPEN_PLACE,
    SPECIAL_MARK,
    DESERTION_NO,
    PUBLIC_NOTICE_NO,
    PUBLIC_NOTICE_START,
    PUBLIC_NOTICE_END,
    IMAGE_URL,
    WEIGHT,
    COLOR_CD,
    PROCESS_STATE,
    API_SOURCE,
    API_SHELTER_NAME,
    API_SHELTER_TEL,
    API_SHELTER_ADDR,
    CREATED_AT,
    UPDATED_AT
) VALUES (
    '라브라도',  -- NAME 추가
    '개',
    '라브라도 리트리버',
    3,
    'F',
    'N',  -- U를 N으로 변경 (중성화 여부 불명은 N으로 처리)
    'AVAILABLE',
    DATE '2025-07-13',
    '경기도 양평군 단월면 고북길 347',
    '구조 당시 은색 목걸이 착용',
    '441417202501214',
    '경기-양평-2025-00279',
    DATE '2025-07-13',
    DATE '2025-07-23',
    'http://openapi.animal.go.kr/openapi/service/rest/fileDownloadSrvc/files/shelter/2025/07/202507131707792.png',
    28.4,
    '검정색',
    '보호중',
    'PUBLIC_API',
    '양평군유기동물보호소',
    '031-770-2337',
    '경기도 양평군 양평읍 농업기술센터길 59-1',
    SYSTIMESTAMP,
    SYSTIMESTAMP
);

COMMIT;