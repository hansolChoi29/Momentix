# Momentix
### "특별한 순간을 예매한다"
Moment + Tix (Tickets) : 특별한 순간을 위한 티켓 예매 서비스

개발 기간 : 2025.09.01 ~ 2025.10.02 (1개월) | BE 4명
## 👥 팀원 소개

| 이름 | 직책 |                 담당                  | GitHub | Blog |
| :---: | :---: |:-----------------------------------:| :---: | :---: |
| 곽지훈 | 팀원 |         S3, 리뷰, 좌석 선점, 즐겨찾기         | [Gwakjihun](https://github.com/Gwakjihun) | [rhkrwlgns](https://rhkrwlgns.tistory.com/) |
| 전재민 | 부팀장 |          Slack, 좌석 선택, 티켓           | [Beforejamni](https://github.com/Beforejamni) | [beforejamn1](https://beforejamn1.tistory.com/) |
| 최재혁 | 팀장 |         공연, 좌석, 기본 검색, 대기열          | [Gemini-kei](https://github.com/Gemini-kei) | [keigemini](https://velog.io/@keigemini/posts) |
| 최한솔 | 서기 | Auth, Elasticsearch, 포인트, CI/CD | [hansolChoi29](https://github.com/hansolChoi29) | [winwin0219](https://winwin0219.tistory.com/) |
---

## 🛠️ 시스템 아키텍처
<img width="1091" height="691" alt="Image" src="https://github.com/user-attachments/assets/f349bd99-9bfe-4c1b-b5d5-9d8531693e5a" />

---

## 🛠 기술 스택

#### Languages
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

#### Frameworks & ORM
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)

#### Security
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
<img src="https://img.shields.io/badge/oauth2-000000?style=for-the-badge&logo=oauth2&logoColor=white">

#### Databases & Search
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/elasticsearch-%230377CC.svg?style=for-the-badge&logo=elasticsearch&logoColor=white)

#### Cloud & Infrastructure
![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)

#### Notification
![Sendgrid](https://img.shields.io/badge/Sendgrid-51A9E3?style=for-the-badge&logo=Sendgrid&logoColor=white)

#### Build & Test
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)
<img src="https://img.shields.io/badge/jmeter-9D1620?style=for-the-badge&logo=jmeter&logoColor=white">

#### Collaboration
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)

---

## 🚀 Getting Started

#### 요구 환경
```
JDK        : 17.0.15 (Amazon Corretto)
MySQL      : 8.0.34
Redis      : 6.2
Gradle     : 8.14.3
Spring Boot: 3.5.5
Docker / Docker Compose
```

#### 실행 방법

1. 클론
```bash
git clone https://github.com/hansolChoi29/Momentix.git
cd Momentix
```

2. 루트 디렉토리에 `.env` 파일 생성
```
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=yourpassword

REDIS_HOST=localhost
REDIS_PORT=6379

JWT_SECRET=your_jwt_secret_key_32bytes!!

AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_BUCKET=your_bucket_name
AWS_REGION=ap-northeast-2

KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
KAKAO_REDIRECT_URI=http://localhost:8080/auth/sign-in/callback/kakao

NAVER_CLIENT_ID=your_naver_client_id
NAVER_CLIENT_SECRET=your_naver_client_secret
NAVER_REDIRECT_URI=http://localhost:8080/auth/sign-in/callback/naver

MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_app_password

ELASTICSEARCH_URL=your_elasticsearch_url
ELASTIC_API_KEY=your_elastic_api_key
```

3. Docker로 MySQL / Redis 실행
```bash
docker compose up -d
```

4. 앱 실행
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

---

## 📋 핵심 기능

#### 공연 정보 탐색 및 조회
- 날짜, 장르, 장소, 인기순 등 다양한 조건 검색 및 필터링
- 공연 상세 정보 조회 (줄거리, 출연진, 시간, 가격, 장소)
- Elasticsearch 기반 자동완성 및 인기 검색어 집계

#### 인증 및 계정
- 이메일 인증 기반 회원가입 (Redis TTL + 쿨다운 처리)
- 카카오 / 네이버 소셜 로그인 (OAuth 2.0)
- JWT Access / Refresh Token 기반 인증

#### 실시간 좌석 선택 예매
- 좌석 배치도 기반 실시간 선택
- Redis 분산 락 + DB 낙관적 락 조합으로 동시성 제어
- Redis Sorted Set 기반 대기열 관리 및 WebSocket 순번 안내

#### 결제 및 티켓 관리
- 결제 완료 및 티켓 발행
- 포인트 적립/사용
- 마이페이지 내 예매 내역 조회

---

## 📈 사용자 이용 흐름도
<img width="802" height="490" alt="Image" src="https://github.com/user-attachments/assets/ae1e6810-4b3d-4467-8d1c-669ebf821ec8" />

---

## 🔩 기술적 의사결정

<details><summary>MySQL</summary>

#### 프로젝트 요구사항
- 좌석 관리, 예매 내역, 결제 정보 등 정합성과 트랜잭션 안정성이 중요한 데이터 처리 필요

| 비교 항목 | MySQL | PostgreSQL |
| :--- | :--- | :--- |
| 트랜잭션 지원 | InnoDB 기반 ACID 지원 | ACID 준수, 고급 트랜잭션 기능 |
| 데이터 정합성 | 외래키·제약조건으로 충분히 보장 | 복잡한 제약조건과 고급 데이터 타입 지원 |
| 성능 | 중소형 규모 | 대규모 시스템 |
| 확장성 | 수직 확장 중심 | 수평 확장 및 병렬 처리 지원 |
</details>

<details><summary>JWT</summary>

#### 프로젝트 요구사항
- 동시 접속자가 급증하는 티켓팅 환경에서 서버 부하 최소화
- 클라이언트와 서버 간 인증 정보를 안전하게 전달

| 비교 항목 | Session | JWT |
| :--- | :--- | :--- |
| 상태 관리 | 서버 메모리 | 클라이언트 쿠키/헤더에 저장 |
| 인증 처리 | 매 요청 DB/세션 조회 필요 | 토큰 서명 검증만으로 인증 |
| 확장성 | 서버 확장 시 세션 공유 필요 | 별도 공유 필요 없음 |
</details>

<details><summary>성인 인증</summary>

#### 프로젝트 요구사항
- 공연 연령 제한 사전 확인 필요
- 초기 비용·운영 부담 최소화, 추후 확장 가능 구조

| 비교 항목 | 네이버 소셜 로그인 회원정보 | KG 이니시스 | 네이버 인증 |
| :---: | :---: | :---: | :---: |
| 본인인증 비용 | 기본 제공 | 40원/건 | 30원/건 |
</details>

<details><summary>대기열 관리</summary>

#### 프로젝트 요구사항
- 대규모 동시 접속 환경에서 공정한 FIFO 대기열 관리
- 실시간 순번 안내 및 예매 가능 인원 동시성 문제 없이 관리
- 차례가 된 사용자에게 알림 누락 없이 안정적으로 전달

| 자료구조 | 주요 역할 | 장점 | 단점 |
| :--- | :--- | :--- | :--- |
| Redis Sorted Set ✅ | 대기열 순서 관리 (FIFO) | Set 기반 중복 방지, `Rank`로 순번 빠른 조회 O(log N) | Score 관리를 위해 접속 시각 저장 필요 |
| Redis String ✅ | 실시간 카운터 및 상태 저장 | `INCR/DECR`로 동시성 문제 없이 카운팅, TTL 자동 만료 | 사용자별 여러 키 관리 필요 |
| Redis Stream ✅ | 예매 가능 알림 전달 | 소비자 그룹으로 병렬 처리, 메시지 보존으로 누락 방지 | 데이터 누적으로 주기적 관리 필요 |
</details>

<details><summary>대기열 순번 안내</summary>

#### 프로젝트 요구사항
- 수만 명의 동시 접속자에게 실시간으로 대기 순번 및 예매 가능 여부 안내
- 양방향 통신 기반 기능 확장 가능성 고려

| 비교 항목 | WebSocket ✅ | SSE |
| :--- | :--- | :--- |
| 통신 방식 | 양방향 (서버 ↔ 클라이언트) | 단방향 (서버 → 클라이언트) |
| 장점 | 실시간 상호작용 가능, 텍스트/이진 데이터 지원 | HTTP 기반 높은 호환성, 자동 재연결 |
| 단점 | 구현 복잡도 높음, 연결 단절 시 수동 재연결 필요 | 클라이언트 요청 없이 서버 전송 불가, 텍스트만 지원 |
</details>

<details><summary>좌석 선점 동시성 제어</summary>

#### 프로젝트 요구사항
- 수만 명이 동시에 특정 좌석을 예매하는 상황에서 중복 판매 방지
- 부하 테스트 결과, 빠른 응답 속도보다 에러 없는 안정성과 데이터 정합성이 더 중요한 지표로 판단

Redis 분산 락이 DB로 향하는 동시 요청을 1차로 줄여 DB 병목을 방지하고, DB 낙관적 락이 최종 데이터 무결성을 검증하는 이중 안전망 구조 채택

| 방식 | p(95) 응답 시간 | TPS | 서버 에러 (100회) |
| :--- | :--- | :--- | :--- |
| 낙관적 락 | 213.41ms | 288.51 | 1회 |
| 분산 락 | 334.60ms | 216.41 | 0회 |
| 조합 방식 ✅ | 359.60ms | 206.13 | 0회 |
</details>

<details><summary>OpenCSV</summary>

#### 프로젝트 요구사항
- 관리자가 공연장 좌석 기본 배치 및 공연별 좌석 등급/가격 구역 등록

| 비교 항목 | OpenCSV | Apache Commons CSV |
| :--- | :--- | :--- |
| POJO 매핑 | 어노테이션 지원 | 수작업 매핑 필요 |
| Spring Boot 통합 | 간단 | 코드 작성 필요 |
| 데이터 처리 규모 | 중소 규모 | 대규모/스트리밍 |
</details>

<details><summary>AWS S3</summary>

#### 프로젝트 요구사항
- 이미지 수 증가에도 안정적 운영, 이미지 트래픽이 서버 성능에 영향을 주지 않아야 함

| 비교 항목 | 서버에 직접 저장 | DB에 저장 | AWS S3 ✅ |
| :--- | :--- | :--- | :--- |
| 확장성/안정성 | 서버 확장 시 데이터 불일치 | DB 백업/복구 비효율 | 뛰어난 확장성/안정성 |
| 서버 성능 | 이미지 요청 증가 시 서버 부하 | DB 성능 저하 | URL만 DB 저장, 서버 부담 없음 |
| 비용 | 비효율적 | DB 저장 공간 고비용 | 스토리지 저비용 |
</details>

---

## 🔑 트러블 슈팅

<details><summary>Social Login - Kakao KOE006 Error</summary>

- **문제**: 카카오 로그인 후 서비스로 정상 리디렉션되지 않고 KOE006 오류 화면 노출
- **원인**: `state` 생성·보관·검증 단계 누락 및 카카오 앱이 비즈 앱으로 전환되지 않음
- **해결**:
  1. 로그인 요청 시 난수 `state`를 생성해 세션에 저장
  2. 콜백 단계에서 세션의 `state`와 파라미터 값을 비교 검증 후 즉시 세션에서 제거
  3. 카카오 앱을 비즈 앱으로 전환
- **결과**: 정상 로그인 및 콜백 처리, DB에 소셜 이메일 정상 저장 확인
</details>

<details><summary>낙관적 락의 UPDATE 쿼리 미실행</summary>

- **문제**: 좌석 상태가 `AVAILABLE`임에도 `SELECT` 쿼리만 실행되고 `UPDATE` 쿼리 미실행
- **원인**: `@Version` 필드 추가 전 기존 데이터의 `version` 컬럼 값이 `null`이어서 JPA 낙관적 락 내부에서 예외 발생 후 롤백
- **해결**:
  ```sql
  UPDATE event_time_reserve_seat SET version = 0 WHERE version IS NULL;
  ```
- **결과**: `UPDATE` 쿼리 정상 실행 확인
</details>

<details><summary>Elasticsearch - Keyword 타입 불일치</summary>

- **문제**: 자동완성 호출 시 필드 타입 오류 발생, 인기 검색어 집계 결과 비어있음
- **원인**:
  1. 자동완성 대상 필드가 `Completion` 타입으로 매핑되지 않음
  2. 인기 검색어 집계 필드가 `text` 타입이라 토크나이징되어 `Terms` 집계 미동작
- **해결**: 인덱스 매핑을 `Completion` / `keyword` 타입으로 수정 후 재색인
- **결과**: 자동완성 및 인기 검색어 Top-N 집계 정상 동작 확인
</details>

<details><summary>Redis Stream - 컨슈머 그룹 생성 시 의도치 않은 키 생성</summary>

- **문제**: 컨슈머 그룹 생성 과정에서 의도하지 않은 쓰레기 Stream 키 생성
- **원인**: 키 값을 받아오는 데이터 타입을 `Object`로 처리해 의도와 다른 키 값 전달
- **해결**: 데이터 타입을 `Object`에서 `String`으로 명시적 변환
- **결과**: 컨슈머 그룹 정상 생성, 쓰레기 키 미생성 확인
</details>

<details><summary>WebSocket - 대기열 순번 알림 누락</summary>

- **문제**: Redis Stream 컨슈머가 메시지 수신 후 WebSocket으로 클라이언트에 알림 미전달
- **원인**: 컨슈머에서 사용하던 WebSocket SessionId와 사용자의 실제 SessionId를 혼동
- **해결**: 사용자의 SessionId 기반으로 비교해 정확한 대상에게 알림 전송하도록 수정
- **결과**: 특정 사용자에게 알림 정상 전달 확인
</details>

---

