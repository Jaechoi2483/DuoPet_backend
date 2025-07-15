import pandas as pd
import json

# 원본 CSV 파일 읽기 (컬럼 구조 확인용)
original_df = pd.read_csv('animal_hospital.csv')
original_columns = original_df.columns.tolist()

# 필터링된 JSON 파일 읽기
with open('filtered_animal_hospitals.json', 'r', encoding='utf-8') as f:
    filtered_data = json.load(f)

# 필터링된 데이터를 원본 CSV 형식으로 변환
new_df = pd.DataFrame(columns=original_columns)

for i, hospital in enumerate(filtered_data):
    # 새로운 행 생성
    row = {col: '' for col in original_columns}
    
    # 필요한 데이터 매핑
    row['번호'] = str(i + 1)
    row['개방서비스명'] = '동물병원'
    row['개방서비스아이디'] = '02_03_01_P'
    row['관리번호'] = str(hospital['management_no'])
    row['인허가일자'] = hospital['license_date']
    row['영업상태구분코드'] = '01'
    row['영업상태명'] = '영업/정상'
    row['상세영업상태코드'] = '0000'
    row['상세영업상태명'] = '정상'
    row['소재지전화'] = hospital['phone']
    row['소재지우편번호'] = hospital.get('postal_code', '')
    row['소재지전체주소'] = hospital['address']
    row['도로명전체주소'] = hospital['road_address']
    row['도로명우편번호'] = hospital.get('postal_code', '')
    row['사업장명'] = hospital['hospital_name']
    row['좌표정보x(epsg5174)'] = str(hospital['x_coordinate'])
    row['좌표정보y(epsg5174)'] = str(hospital['y_coordinate'])
    row['최종수정시점'] = '2025-07-14 12:00:00'
    row['데이터갱신구분'] = 'U'
    row['데이터갱신일자'] = '2025-07-14 12:00:00'
    row['업무구분명'] = '동물병원'
    
    # DataFrame에 추가
    new_df = pd.concat([new_df, pd.DataFrame([row])], ignore_index=True)

# CSV 파일로 저장
new_df.to_csv('filtered_animal_hospitals.csv', index=False, encoding='utf-8-sig')
print(f"필터링된 {len(new_df)}개의 병원 데이터를 CSV 파일로 저장했습니다.")

# 샘플 데이터 확인
print("\n첫 5개 병원 정보:")
for col in ['번호', '사업장명', '소재지전체주소', '소재지전화', '좌표정보x(epsg5174)', '좌표정보y(epsg5174)']:
    print(f"{col}: {new_df[col].head().tolist()}")