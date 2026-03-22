# Parking-Guard
**세그먼테이션 기반 객체 탐지를 활용한 불법 주·정차 민원 자동화 시스템** 입니다.

기존 불법 주·정차 민원은 사람이 직접 확인하고 판단해야 하는 구조라 시간도 오래 걸리고, 처리 부담도 크다는 점에서 출발했습니다.  

---
## 프로젝트 개요
기존 불법 주·정차 신고 시스템에서는 신고자가 직접 불법 유형을 선택해야 하는데,
일반 시민 입장에서는 정확한 유형을 알기 어렵거나 아무 유형이나 선택해 제출하는 경우도 발생할 수 있습니다.

### Parking-Guard는 신고자가 촬영한 이미지를 분석해 불법 주·정차 유형을 시스템이 대신 판단하도록 설계했습니다.

이를 통해 **신고 과정은 단순**해지고, 공무원은 유형 분류에 드는 부담을 줄일 수 있도록 했습니다.

신고 과정에서 불필요한 선택 단계를 줄여 누구나 쉽게 신고할 수 있도록 UI를 단순하게 구성했습니다.

### 자동 분류가 가능한 경우에는  
- 신고자가 유형을 선택하지 않아도 바로 접수가 가능하며,  
- 필요한 경우에만 최소한의 선택을 하도록 설계했습니다.

### 무분별한 중복 신고를 방지하기 위해 다음과 같은 제한 로직을 적용했습니다.
- 동일 차량이 같은 날짜에 같은 유형으로 같은 위치에서 이미 신고된 경우
  -> 추가 신고 불가
- 불법 주·정차가 아닌 일반 주차 차량으로 판단되는 경우
  -> 신고 접수 차단

이를 통해 신고 신뢰도를 높이고, **행정 처리 부담을 줄이는 것을 목표**로 했습니다.

---
## 사용 기술 및 구현 환경
- **서버(백엔드)**: FastAPI  
  이미지 업로드 및 분석 요청을 처리하고,
  AI 추론 결과를 애플리케이션에 전달하는 API 서버를 구성했습니다.

- **애플리케이션(프론트엔드)**: Android Studio  
  사용자가 불법 주·정차를 신고하고,
  분석 결과를 확인할 수 있도록 모바일 애플리케이션을 구현했습니다.

- **AI 모델**: YOLOv11x-seg  
  YOLOv11x-seg 모델을 기반으로 하이퍼파라미터 튜닝을 진행했으며,
  개인 데이터셋으로 추가 학습한 모델(pt)을 사용했습니다.

- **데이터셋**: 개인 구축 데이터셋  
  불법 주·정차 관련 이미지 약 3000 장을 직접 수집하고,
  폴리곤 기반 세그멘테이션 라벨링을 수행했습니다.

## 차량 번호판 인식 처리

세그멘테이션 기반 객체 탐지를 통해
차량 및 번호판 영역을 탐지한 뒤,
번호판 영역만 별도로 크롭하여 OCR 처리를 수행했습니다.

OCR 단계에서는 네이버 OCR API를 활용해
차량 번호 정보를 추출했으며,
이를 중복 신고 방지 로직에 활용했습니다.

---

## 객체 탐지 모델 비교
객체 탐지 모델을 선정할 때  
**구조와 특성이 다른 세 가지 세그먼테이션 모델** 을 기준으로 비교 실험을 진행했습니다.

<table>
  <tr>
    <td align="center">
      <img width="291" height="284"
           src="https://github.com/user-attachments/assets/57daabdb-5787-4298-be11-b835e3121689" />
      <br/>
      <b>YOLO-SEG</b>
    </td>
    <td align="center">
      <img width="291" height="284"
           src="https://github.com/user-attachments/assets/6d48fa79-c95d-4012-9962-7c5be391492b" />
      <br/>
      <b>YOLACT</b>
    </td>
    <td align="center">
      <img width="291" height="284"
           src="https://github.com/user-attachments/assets/7cfad2ae-5b81-424a-97ef-31a993094a5a" />
      <br/>
      <b>Mask R-CNN</b>
    </td>
  </tr>
</table>

### 동일한 이미지를 입력해 추론을 수행했을 때,
- YOLO-SEG: 가장 짧은 추론 시간이 관찰되었습니다.
- YOLACT: YOLO-SEG 대비 상대적으로 긴 추론 시간이 소요되었습니다.
- Mask R-CNN: 출력까지의 지연 시간이 가장 길게 관찰되었습니다.

실시간 적용 가능성을 고려하여,  
본 프로젝트에서는 YOLO-SEG 계열 모델을 우선적으로 선정한 뒤 정확도와 추론 속도를 기준으로 추가 비교 실험을 진행했습니다.

그 결과,
### **YOLOv11x-seg** 모델을 최종 모델로 선정했습니다.

---
# 애플리케이션 실행 화면
<table>
  <tr>
    <td align="center">
      <img width="204" height="431" alt="image" src="https://github.com/user-attachments/assets/446e825f-171e-4ac6-b769-3283550469bf" /><br/>
      <b>메인 화면</b>
    </td>
    <td align="center">
      <img width="204" height="431" alt="image" src="https://github.com/user-attachments/assets/28c93678-bdb3-42d8-8e86-2e30e62e8515" /><br/>
      <b>신고 화면</b>
    </td>
    <td align="center">
      <img width="214" height="420" alt="image" src="https://github.com/user-attachments/assets/21ad6474-886b-4d23-983d-a2a9dfbf385d" /><br/>
      <b>촬영 결과 화면</b>
    </td>
  </tr>
</table>

<table>
  <td align="center">
    <img width="392" height="200" alt="image" src="https://github.com/user-attachments/assets/fd8db7e0-c103-467b-85a8-8bc62306c974" /><br/>
    <b>촬영 화면</b>
  </td>
</table>

---
## 정리
Parking-Guard는
불법 주·정차 신고 과정에서 발생하는
신고자의 판단 부담과 행정 처리 비효율을 줄이기 위해 설계한 프로젝트입니다.

객체 탐지와 세그멘테이션 결과를
유형 분류와 차량 식별(OCR)까지 연결해,
실제 신고 흐름에 적용 가능한 형태로 구현했습니다.
