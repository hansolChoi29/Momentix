# 프로젝트 : Momentix
### "특별한 순간을 예매한다"
Moment + Tix (Tickets) : “순간 + 티켓” → 특별한 순간을 위한 티켓. 



## version
- JDK : 17.0.15 (Amazon Corretto)
- MySQL : 8.0.34
- Gradle : 8.14.3
- Spring Boot : 3.5.6
- Docker / Docker Compose

- Redis 6.x


---

## bulid
1. 깃 클론
```
git clone https://github.com/hansolChoi29/Momentix.git
```
2. `.env` 환경변수 세팅


</br>

## 🔦 핵심기능

#### 공연 정보 탐색 및 조회
- 다양한 조건 검색 및 필터링   <br>
  - 날짜, 장르, 장소, 인기순 등<br>
 - 공연 상세 정보 조회<br>
   - 줄거리, 출연진, 시간, 가격, 장소 정보<br>
- 관련 공연 추천/큐레이션<br>

#### 실시간 좌석 선택 예매
- 좌석 배치도 기반 실시간 선택
- 좌석 점유 및 제한 시간
- 예매 수량 및 가격 정보 확인

#### 결제 및 티켓 내역 관리
- 다양한 결제 수단 연동
- 결제 완료 및 티켓 발행
- 마이페이지 내 예매 내역 조회

## 📈 사용자 이용 흐름도

<img width="802" height="490" alt="Image" src="https://github.com/user-attachments/assets/ae1e6810-4b3d-4467-8d1c-669ebf821ec8" />

## 🛠️ 시스템 아키텍처

<img width="1091" height="691" alt="Image" src="https://github.com/user-attachments/assets/f349bd99-9bfe-4c1b-b5d5-9d8531693e5a" />


## 🛠 기술 스택
#### 📋 Languages
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)

#### 🎋 ORM & Frameworks
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)

#### ☁️ Cloud & Infrastructure
![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)

#### 🔐 Security
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)
<img src="https://img.shields.io/badge/oauth2-000000?style=for-the-badge&logo=oauth2&logoColor=white"> 

#### 💾 Databases & Search Engine
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/elasticsearch-%230377CC.svg?style=for-the-badge&logo=elasticsearch&logoColor=white)

#### 📧 Notification Service
![Sendgrid](https://img.shields.io/badge/Sendgrid-51A9E3?style=for-the-badge&logo=Sendgrid&logoColor=white)

#### 🖥️ Backend Development
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)

#### 📟 Test
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)
<img src="https://img.shields.io/badge/jmeter-9D1620?style=for-the-badge&logo=jmeter&logoColor=white"> 

#### 🎨 Design & Collaboration Tools
![Canva](https://img.shields.io/badge/Canva-%2300C4CC.svg?style=for-the-badge&logo=Canva&logoColor=white)
![Figma](https://img.shields.io/badge/figma-%23F24E1E.svg?style=for-the-badge&logo=figma&logoColor=white)
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=for-the-badge&logo=slack&logoColor=white)
![Zoom](https://img.shields.io/badge/Zoom-2D8CFF?style=for-the-badge&logo=zoom&logoColor=white)
<img src="https://img.shields.io/badge/erd cloud-7B00FF?style=for-the-badge&logo=erd&logoColor=white"> 

## 🔩 기술적 의사결정

<details><summary>📚MySQL
</summary>

#### 프로젝트 요구사항

- 티켓 예매 시스템에서 좌석 관리, 예매 내역, 경제 정보 등
- 정합성과 트랜잭션 안정성이 중요한 데이터 처리 필요


| 비교사항| MySQL| PostgreSQL| 
| :--- | :--- | :--- |
|트랜잭션 지원|InnoDB기반 ACID지원  | ACID준수, 고급 트랜잭션 기능  | 
|데이터 정합성|외래키·제약조건으로 충분히 보장  | 복잡한 제약조건과 고급 데이터 타입 지원 | 
|성능|중소형 규모  |  대규모 시스템| 
|확장성| 수직 확장 중심 |수평 확장 및 병렬 처리 지원  | 

</details>

<details><summary>🔒JWT</summary>


#### 프로젝트 요구사항
 - 동시 접속자가 급증하는 티켓팅 환경에서도 서버 부하를 최소화해야 함
 - 클라이언트와 서버 간 인증 정보를 안전하게 전달할 수 있어야 함

| 비교 항목| Session| JWT|
| --- | --- | --- |
| 상태 관리|서버 메모리|클라이언트 쿠키/헤더에 저장|
|인증 처리|매 요청 DB/세션 조회 필요 |토큰 서명 검증만으로 인증|
|확장성/부하|서버 확장 시 세션 공유 필요|별도 공유 필요 없음|

</details>

<details><summary> 🙈성인 인증</summary>

#### 프로젝트 요구사항
- 일반 공연 연령 제한 법적 기준 일관 부재 → 사전 연령·성인 여부 확인 필요함
- 초기 비용·운영 부담 최소화 요구, 추후 인증으로 확장할 수 있어야 함

| 비교사항| 네이버 소셜 <br>로그인 회원정보| KG 이니시스 | 네이버 인증|
| :---: | :---: | :---: |:---:|
| 본인인증 비용 | 기본제공 | 40원/건 | 30원/건|

</details>

<details><summary>🧑‍🧑‍🧒‍🧒대기열 관리</summary>

#### 프로젝트 요구사항
- 대규모 동시 접속 환경에서 공정한 선입선출(FIFO) 대기열을 관리해야 함.
- 사용자에게 실시간으로 자신의 순번을 알려주고, 예매 가능 인원을 동시성 문제 없이 정확히 관리해야 함.
- 자신의 차례가 된 사용자에게 예매 페이지로 이동하라는 알림을 누락 없이 안정적으로 전달해야 함

| 사용된 자료구조 | 주요 역할 | 장점 | 단점 |
| :--- | :--- | :--- | :--- |
| Redis Sorted Set ✅ | 대기열 순서 관리 (FIFO) | ∙ Set 기반으로 중복 입장 방지<br>∙ `Rank` 기능으로 순번 빠른 조회 ($O(\log N)$) | ∙ Score 관리를 위해 접속 시각 저장 필요<br>∙ 동시 접속 처리를 위한 추가 로직 필요 |
| Redis String ✅ | 실시간 카운터 및 상태 저장 | ∙ `INCR/DECR`로 동시성 문제 없이 카운팅<br>∙ `TTL`로 상태 자동 만료 관리 용이 | ∙ 사용자별로 여러 키를 관리해야 할 수 있음 |
| Redis Stream ✅ | 예매 가능 알림 전달 | ∙ 소비자 그룹으로 병렬 처리 및 확장성 우수<br>∙ 메시지 보존으로 재접속 시 누락 방지 | ∙ 데이터가 계속 쌓여 주기적인 관리 필요<br>∙ 복잡한 트랜잭션 보장 어려움 |

</details>

<details><summary>👥대기열 순번 안내</summary>

#### 프로젝트 요구사항
- 수만 명의 동시 접속자가 발생하는 대기열 환경에서, 각 사용자에게 자신의 대기 순번과 예매 가능 여부를 실시간으로 안정적으로 알려주어야 함.
- 향후 관리자 공지, 실시간 좌석 현황 업데이트 등 양방향 통신이 필요한 기능 확장 가능성을 고려해야 함.

| 비교 항목 | WebSocket ✅ | SSE (Server-Sent Events) |
| :--- | :--- | :--- |
| 통신 방식 | 양방향 통신 (서버 ↔ 클라이언트) | 단방향 통신 (서버 → 클라이언트) |
| 주요 장점 | ∙ 실시간 상호작용 가능<br>∙ 텍스트 및 이진 데이터 전송 지원 | ∙ HTTP 기반으로 높은 호환성<br>∙ 내장 자동 재연결 기능 |
| 주요 단점 | ∙ 구현 복잡도가 상대적으로 높음<br>∙ 연결 단절 시 수동 재연결 처리 필요 | ∙ 클라이언트의 요청 없이는 서버로 데이터 전송 불가<br>∙ 텍스트 데이터만 지원 |

</details>

<details><summary>💺좌석 선점 동시성 제어</summary>

#### 프로젝트 요구사항
- 수만 명의 사용자가 동시에 특정 좌석을 예매하려는 상황에서 데이터 정합성을 유지하고(중복 판매 방지), 안정적인 서비스를 제공해야 함.
- 부하 테스트 결과, 단순히 빠른 응답 속도보다 에러 없는 안정적 확장성과 데이터 정합성이 훨씬 더 중요한 지표였음.
- Redis 분산 락이 DB로 향하는 동시 요청을 1차로 줄여 DB 병목을 방지하고, DB의 낙관적 락이 최종 데이터 무결성을 검증하여 이중 안전망 구조를 형성함.
  
| 방식 | p(95) 응답 시간 | TPS (reqs/s) | 서버 에러 (100회) |
| --- | --- | --- | --- |
| 낙관적 락 | 213.41 ms | 288.51 | 1 회 (DB 병목으로 실패) |
| 분산 락 | 334.60 ms | 216.41 | 0 회 |
| 조합 방식 | 359.60 ms | 206.13 | 0 회 |

</details>

<details><summary> 📜OpenCSV </summary>

#### 프로젝트 요구사항
- 관리자가 공연장의 좌석 기본 배치 등록
- 공연별 좌석 등급, 가격 구역 등록



| 비교 항목| OpenCSV| Apache Commons CSV|
| :--- | :--- | :--- |
| POJO(Plan Old Java Object) 매핑| CsvBindByName 등<br> 어노테이션 지원 | 수작업 매핑 필요|
| 설정 및 통합|  Spring Boot 통합 간단| 코드 작성 필요|
| 데이터 처리 규모| 중소 규모 처리| 대규모 / 스트리밍 처리|


</details>


<details><summary> 📁AWS S3
</summary>

#### 프로젝트 요구사항
- 확장성 : 이미지 수가 증가하더라도 안정적으로 서비스를 운영할 수 있어야 함.
- 서버 성능 : 이미지 파일 트래픽이 서버의 성능에 영향을 주지 않아야 함.

| 비교 항목| 서버에 직접 저장| 데이터베이스에 저장| AWS S3 |
| --- | --- | --- |--- |
| 확장성/안정성| 서버 확장 시 데이터 불일치|DB가 무거워져 백업/복구 비효율 |뛰어난 확장성/안정성|
| 서버 성능| 이미지 요청↑, 서버 부하|DB가 커지며 심각한 성능 저하 |URL 주소만 DB에 저장 서버 부담 X|
| 비용 효율성| 비효율적 비용| DB 저장 공간 고비용|스토리지 비용 저비용|

</details>

## 🔑 트러블 슈팅

<details><summary>Social Login - Kakao KOE006 Error</summary>

- 문제 파악: 카카오 로그인 후 서비스로 정상 리디렉션되지 않고, KOE006 오류 화면이 나타났습니다. `state` 값 검증 로직이 없어 CSRF 공격에 취약한 문제도 발견되었습니다.
- 원인 추론: `state` 생성·보관·검증 단계가 누락되었고, 카카오 앱이 '비즈 앱'으로 전환되지 않아 Redirect URI 동작에 제약이 있었습니다.
- 해결 과정:
    1. 로그인 요청 시 난수 `state`를 생성해 세션에 저장했습니다.
    2. 콜백 단계에서 세션의 `state` 값과 파라미터로 전달된 값을 비교하여 검증 후, 즉시 세션에서 제거했습니다.
    3. 카카오 앱을 비즈 앱으로 전환하여 Redirect URI 문제를 해결했습니다.
- 결과: 오류 없이 정상적으로 로그인 및 콜백 처리가 되었고, DB에 소셜 이메일이 정상적으로 저장되는 것을 확인했습니다.

</details>

<details><summary>낙관적 락의 UPDATE 쿼리 미실행</summary>

- 문제 파악: 좌석 상태가 `AVAILABLE`임에도 불구하고, API 호출 시 `SELECT` 쿼리만 실행되고 좌석 상태를 변경하는 `UPDATE` 쿼리가 실행되지 않았습니다.
- 원인 추론: 트랜잭션이 커밋되지 않고 롤백되었음을 의미. 로그 분석 결과, 엔티티에 `@Version` 필드를 추가하기 전의 기존 데이터들은 `version` 컬럼 값이 `null`이었습니다. JPA의 낙관적 락 메커니즘은 `version` 값이 숫자일 것을 기대하는데, `null`이어서 내부적으로 예외가 발생하며 롤백된 것이었습니다.
- 해결 과정: 기존 모든 데이터의 `version` 컬럼 값을 JPA가 기대하는 초깃값인 `0`으로 설정하는 SQL 업데이트 쿼리를 실행했습니다.
    ```sql
    UPDATE event_time_reserve_seat SET version = 0 WHERE version IS NULL;
    ```
- 결과: `version` 필드가 정상 값을 갖게 되자, JPA의 낙관적 락이 의도대로 동작하여 `UPDATE` 쿼리가 정상적으로 실행되었습니다.

</details>

<details><summary>Elasticsearch - Keyword 타입 불일치</summary>

- 문제 파악: 자동완성(Suggest) 기능 호출 시 필드 타입 오류가 발생했으며, 인기 검색어 집계 결과가 비어있는 현상이 있었습니다.
- 원인 추론:
    1. 자동완성 대상 필드가 `Completion` 타입으로 매핑되지 않았습니다.
    2. 인기 검색어 집계 대상 필드가 `text` 타입으로 되어 있어, 문자열이 단어 단위로 쪼개져(토크나이징) `Terms` 집계가 정상 동작하지 않았습니다. (정확한 집계를 위해서는 `keyword` 타입 필요)
- 해결 과정:
    1. 인덱스 매핑을 점검해 `Completion` 타입으로 수정하였습니다.
    2. 상수·매핑·색인 JSON의 필드명을 일치시키고, 인덱스를 재생성 후 데이터를 재색인하였습니다.
    3. 집계 필드를 `Keyword` 타입으로 고정하여 문자열이 전체 값 그대로 집계되도록 수정하였습니다.
- 결과: 자동완성 제안이 정상적으로 동작하고, 인기 검색어 Top-N 집계 결과가 정확히 출력되었습니다.

</details>

<details><summary>Redis Stream - 컨슈머 그룹 생성 시 의도치 않은 키 생성</summary>

- 문제 파악: Redis Stream 컨슈머 그룹 생성 과정에서 의도하지 않은 쓰레기(Stream) 키가 생성되는 현상이 발생했습니다.
- 원인 추론: 컨슈머 그룹 생성 시 Stream 키 값을 받아오는 데이터 타입을 `Object`로 처리하고 있어, 의도와 다른 키 값이 전달될 가능성을 확인했습니다.
- 해결 과정: 키를 전달받는 과정에서 데이터 타입을 `Object`에서 `String`으로 명시적으로 변환하여 테스트했습니다.
- 결과: 올바르게 컨슈머 그룹이 생성되고 쓰레기(Stream) 키가 더 이상 생성되지 않음을 확인했습니다.

</details>

<details><summary>WebSocket - 대기열 순번 알림 누락</summary>

- 문제 파악: Redis Stream 컨슈머가 메시지를 수신해 WebSocket으로 클라이언트에 알림을 보내야 하지만, 알림이 전달되지 않음을 확인했습니다.
- 원인 추론: Redis Stream 컨슈머가 메시지를 올바르게 구독하고, WebSocket 연결도 정상이었습니다. 특정 사용자에게 메시지를 보내는 식별 로직에 문제가 있을 것으로 추정했습니다.
- 해결 과정: 컨슈머에서 사용하던 WebSocket SessionId와 사용자의 실제 SessionId를 혼동한 것을 확인했습니다. 컨슈머에서 메시지를 보낼 때, 사용자의 SessionId를 기반으로 비교하여 정확한 대상에게 알림을 보내도록 수정했습니다.
- 결과: WebSocket에서 특정 사용자에게 알림이 정상적으로 전달되는 것을 확인했습니다.

</details>




## 👥 팀원 소개
| 이름| 직책 | Github | blog |
| :---: | :---: | :---: | :---: |
| 곽지훈 | 팀원| [Gwakjihun](https://github.com/Gwakjihun) |[rhkrwlgns](https://rhkrwlgns.tistory.com/)|
| 전재민 | 부팀장 | [Beforejamni](https://github.com/Beforejamni) |[beforejamn1](https://beforejamn1.tistory.com/)|
| 최재혁 | 팀장 | [Gemini-kei](https://github.com/Gemini-kei)|[keigemini](https://velog.io/@keigemini/posts)|
| 최한솔 | 서기 | [hansolChoi29](https://github.com/hansolChoi29) |[winwin0219](https://winwin0219.tistory.com/)|
