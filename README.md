🍗 프로젝트: 치킨러쉬 (Chicken-Rush)
"배민이 견딘 20만 트래픽, 나도 견딜 수 있을까?" > 1만 개의 쿠폰을 차지하려는 4만 명의 동시 접속 상황을 가정하고, 병목 현상을 해결해 나가는 쿠폰 발급 시스템 고도화 프로젝트입니다.

 1. 프로젝트 동기
우연히 배민에서 과거에 선착순 치킨 쿠폰 이벤트 당시  순식간에 엄청난 트래픽(동시에 많은트래픽을 서버가 견디지 못하고 터지게되면서
배민의 레거시 시스템을 점진적으로  변경해가며 20만트래픽까지 견딜수있는 견고한 아키텍처로 완성했던 과정의 이야기를 보며 재밌기도했고 너무 멋있어서 나도경험해보고 싶다는 생각이 들었지만?
일단 내가 서비스를 만들었다한들 20만 트래픽은 개뿔 20명도 안들어오기에  부하테스트로 혼자 대용량 트래픽을 떄려맞는 시나리오를 만들어 발생하는 병목현상을 단계별로 고도화시켜 결과변화를 보며 경험하기위해 시작.
<img width="1213" height="684" alt="image" src="https://github.com/user-attachments/assets/754c46d9-f3bf-478e-88f5-606b76983a66" />

-출처 : 우아한 테크 ( 대략 레거시를 → msa → 디비이관  → NoSql 사용(Redis)→ sns/sqs 메시지큐 → CQRS 패턴 으로 고도화했다는 내용)

🛠 2. Tech Stack
<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=Kotlin&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=Spring-Boot&logoColor=white"> <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=Spring-Boot&logoColor=white"> <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white"> <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=PostgreSQL&logoColor=white"> <img src="https://img.shields.io/badge/k6-7D64FF?style=for-the-badge&logo=k6&logoColor=white">

시나리오 - 1만개의 치킨쿠폰을 받기위해 3만~4만명이 동시에 몰린상황

쿠폰과 내역 Entity 

<img width="449" height="478" alt="image 2" src="https://github.com/user-attachments/assets/ef2d10b4-270d-4b22-b186-b45765a1ecb9" /> <img width="522" height="633" alt="image 1" src="https://github.com/user-attachments/assets/e56318f8-6a12-4000-8dd9-7779f6b0a692" /> 

Phase-1 구조
쿠폰 발급 API,서비스

<img width="810" height="529" alt="image 3" src="https://github.com/user-attachments/assets/262cbfb1-ba79-49b7-9bd9-9479b9994ef5" />
<img width="609" height="467" alt="image 4" src="https://github.com/user-attachments/assets/1ce64841-7e7b-4788-a554-290b9afc9764" />

기본적인 쿠폰발급 api 입니다. 이상태에서 100명이  30초동안  35,000~40,000번 쿠폰발급 호출

<img width="816" height="334" alt="image 5" src="https://github.com/user-attachments/assets/aefab3b7-c64d-43ba-9584-09a71551cf3b" />

**Phase 1 결과 분석:**

**성능:**

- **TPS (초당 처리량)**: 753 req/s
- **평균 응답시간**: 6.14ms
- **성공률**: 70% (26,403건 성공 / 11,256건 실패)

**문제점 발견:**

**1. 성공률 70% - 30%가 실패**

- 11,256건이 400/409 에러
- 아마 "쿠폰 품절" 또는 "중복 발급" 에러

<img width="888" height="97" alt="image 6" src="https://github.com/user-attachments/assets/9b1b8ddc-c6c7-4471-a13a-968f743b1505" />

쿠폰 테이블에서 수량 만개는 다 발급됨. 하지만… 
CouponIssues 테이블  데이터 : 26404 → 11,256건 실패

<img width="829" height="193" alt="image 7" src="https://github.com/user-attachments/assets/ab1cdc44-022f-47b6-9369-a6648257283a" />

바닐라인데도 생각보다 성공률이 높네요.. 근데 중복발급이 엄청나다.. 실서비스였으면 ㅎㄷㄷ; 

**문제점:**

- 동시성문제(중복발급) 쿠폰은 1만개인데  발급된건 2만6천건 뭐임? (실화였으면 1억정도 날림)

## Phase-2 시작

개선해야할점 : DB LOCK 으로  쿠폰 중복발급 개선

참고자료 : https://mrxx.tistory.com/entry/%EC%9D%B4%EC%BB%A4%EB%A8%B8%EC%8A%A4-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8-DB-%EB%8F%99%EC%8B%9C%EC%84%B1-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0%ED%95%98%EA%B8%B0-1

비관적 락 이란 : 데이터 충돌이 무조건 일어난다고 가정하고 누가 해당쿼리를 이용중이면 다음사람은 기다려야 한다고한다. ( 동시에 접근을 하지 못하고 순서대로)

<img width="487" height="131" alt="image 8" src="https://github.com/user-attachments/assets/363cb211-bf26-48a5-ab7f-de344384017e" />
<img width="501" height="82" alt="image 9" src="https://github.com/user-attachments/assets/895c9bdc-40fb-4887-991e-015cefa33eb3" />

서비스단의 쿠폰조회를 변경해줬다. 중복은 막혔겠지? 테스트해보자.

<img width="813" height="284" alt="image 10" src="https://github.com/user-attachments/assets/b09f3152-c27b-4b85-aa55-b0b13b664d88" />

26404 → 1만건으로 중복발급이 막힌걸 볼수있다.

<img width="814" height="117" alt="image 11" src="https://github.com/user-attachments/assets/b7d5f339-9bb1-4d33-b9e5-2754b1cc9406" />
<img width="595" height="287" alt="image 12" src="https://github.com/user-attachments/assets/18fe5ebd-7022-425d-a2db-e25dacf5c5cb" />

희안하게 평균 응답시간도 약간 빨라졌지만 아직 느린속도.

동시성문제는 해결했지만..근데 낙관적 LOCK도 문제가있다고한다.

1명씩 처리하기때문에 더많은 트래픽이 몰릴경우 대기시간이 점차 늘어난다고한다.

## Phase-3

LOCK 제거후 레디스 도입

<img width="897" height="835" alt="image 13" src="https://github.com/user-attachments/assets/a34f4a64-e605-4208-8066-ddb7ee90a2e1" />

레디스로 변경하였고 인메모리 db다 보니 빠른 연산속도로 중복체크와 재고차감을 진행 그리고 

쿠폰 발행은 따로 비동기 처리 (그리고 뭔가 쿠폰발행개수가 안맞아서 Lua script를 사용했다..)

- 레디스로 중복체크와,재고차감을 따로하기때문에 원자성 보장이안되서 저렇게 해줘야한답니다.쩔수래요
- 참고자료 : https://velog.io/@yin/Redis-%EC%8A%A4%ED%94%84%EB%A7%81%EC%97%90%EC%84%9C-%EB%A0%88%EB%94%94%EC%8A%A4-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-%EC%9B%90%EC%9E%90%EC%84%B1%EC%9D%84-%EB%B3%B4%EC%9E%A5%ED%95%98%EB%8A%94-Redisson-Lua
- 

<img width="540" height="161" alt="image 14" src="https://github.com/user-attachments/assets/23ac6ea8-2c34-47da-9440-475f7715743b" />
<img width="823" height="287" alt="image 15" src="https://github.com/user-attachments/assets/b689e23a-5a01-4b77-ba31-50f1f3563a0f" />

테스트 결과 

<img width="713" height="247" alt="image 16" src="https://github.com/user-attachments/assets/293ebee8-87df-4296-a74d-c2c1205e4323" />

### 테스트 결과 응답시간이 (4.14ms → 2.98ms) 40%이상 빨라짐

- Phase 1: 순진한 구현 (JPA만) → 중복 발급 문제
- Phase 2: DB Lock 추가 → 정합성 해결, 느림
- Phase 3: Redis 도입 → 빠르고 정확

3. 고도화 단계별 성능 변화 (TPS & Latency)

🔴 Phase 1: 순진한 구현 (JPA Only)
방식: 기본적인 JPA 엔티티 조회 및 저장

결과: 성공률 70% (30% 실패 및 심각한 중복 발급 발생)

분석: 동시성 제어가 전혀 되지 않아 1만 개 쿠폰이 2.6만 개나 발급됨 (비즈니스적 대참사).

🟠 Phase 2: 비관적 락 (Pessimistic Lock) 적용
방식: DB 수준의 Lock을 걸어 순차적 처리 유도

결과: 정합성 100% 해결 (정확히 1만 개 발급)

분석: 중복 발급은 막았으나, DB 대기 시간으로 인해 트래픽이 몰릴수록 응답 시간이 늘어나는 한계 노출.

🟢 Phase 3: Redis + Lua Script 도입 (최종)
방식: Redis 인메모리 연산 + Lua Script를 통한 원자성 보장 + 비동기 DB 저장

결과: 평균 응답 시간 약 40% 개선 (4.14ms → 2.98ms)

분석: 인메모리의 빠른 속도와 Lua Script의 원자적 처리로 성능과 정합성 두 마리 토끼를 잡음.
