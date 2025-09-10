# ERP↔MES 증분 동기화 규약 & 커서 정책 (Draft v0.9)

본 문서는 ERP(Mock) ↔ MES 간 증분 동기화의 키 규약, 시간/정렬/필터, 커서 전진, 삭제(tombstone), 예외 처리(Unknown), 로깅, 구성 파라미터, 회귀 테스트 방법을 정의한다. 이 문서에 정의된 상수를 코드에 상수화하고, 동작은 자동화 테스트로 회귀한다.

---

## 1) 범위와 용어

- 스트림: items, boms, plans(선택) 각 동기화 대상별 독립 커서 운용
- 증분 기준: updatedAt(UTC)
- 커서: tb_sync_cursor.last_synced_at
- HOP: 하나의 사이클에서 next since로 연속 페이지를 소진하는 내부 루프

---

## 2) 시간/정밀도/필터

- 표준 시간대: UTC 고정
- 정밀도: 시스템이 지원하는 최대 정밀도 사용(나노/마이크로/밀리초)
- 증분 필터: 서버 측 필터는 반드시 다음 부등식을 따른다
  - $updatedAt \ge updatedSince$
- 정렬
  - 1차: updatedAt ASC
  - 2차(타이 브레이커): 자연키 ASC (예: bomId, itemId, planId)

권장 규칙
- 응답 페이지는 정렬 조건을 보장한다.
- 응답 개수 제한은 size(또는 max-rows)로 제어한다.

---

## 3) 키/식별자 규약

- itemId: 영문/숫자/하이픈 허용. 예) ITEM-1001, RM-0001
- bomId: "BOM-" + itemId + "|" + revision + "|" + alt
  - 기본 alt: STD
  - 예) BOM-ITEM-1001|A|STD
- 자연키
  - tb_item: (item_id)
  - tb_bom_header: (item_id, revision, alt_code) 또는 (bom_id)
  - tb_bom_line: (bom_id, line_no)
  - tb_plan: (plan_id) 또는 (item_id, due_date, revision)

---

## 4) 증분 API 계약

### 4.1 공통
- 요청 파라미터: updatedSince(UTC ISO8601), page/size 또는 max-rows
- 응답: 리스트(정렬 보장), tombstone 포함

부등식
- 서버 필터는 $updatedAt \ge updatedSince$ 를 준수한다.

정렬
- updatedAt ASC, 타이 브레이커 자연키 ASC

### 4.2 예시 요청
- Items
  - GET /items?updatedSince=2025-09-07T13:23:30Z
- BOMs
  - GET /boms?updatedSince=2025-09-07T13:12:09Z
- Plans(선택)
  - GET /plans?page=0&size=100&updatedSince=1970-01-01T00:00:00Z

---

## 5) 커서 전진 정책

스트림별 커서 키
| Stream | Cursor Key | 비고 |
|---|---|---|
| Items | erp_items | |
| BOMs | erp_boms | |
| Plans | erp_plans | 선택 |

전진 알고리즘
1) 각 HOP에서 수신 리스트의 최대 시각 $maxTs$ 계산
2) 해당 페이지 적용 후 내부 nextSince는 아래와 같이 설정
   - $nextSince = maxTs + \epsilon$
3) 더 이상 수신이 없으면(got=0) 사이클 종료
4) 사이클 종료 시 외부 커서(last_synced_at)는 마지막으로 소비한 $maxTs$를 저장
   - 다음 사이클 첫 호출의 since는 $last\_synced\_at$ 이며 서버 필터는 $updatedAt \ge since$

정밀도별 $\epsilon$
- 나노초 환경: $\epsilon = 1\text{ns}$
- 마이크로초 환경: $\epsilon = 1\mu s$
- 밀리초 환경: $\epsilon = 1ms$

주의
- 서버는 $ \ge $ 필터, 클라이언트는 내부 HOP에서만 $+\epsilon$을 적용해 경계 중복을 방지한다.
- 외부 커서 값은 원시 $maxTs$(가공 전)를 저장하여 가독성과 추적성을 유지한다.

---

## 6) 삭제(tombstone) 정책

- 물리 삭제 금지
- 논리 삭제: isDeleted=true, updatedAt=now(UTC), deletedAt=now(선택)
- 헤더 삭제 시 라인은 정책에 따라 함께 isDeleted로 표기하거나 조회 시 제외
- ERP 응답에 tombstone을 포함(필터/정렬 규칙 동일)

MES 반영
- deletes 카운트는 tombstone 소비 시 증가
- DB 반영: 헤더/라인 is_deleted=1(설계에 따름), updated_at 동기

---

## 7) 예외 정책(Unknown Component)

- 엄격 모드(strict)
  - 조건: componentId가 tb_item에 없을 때
  - 동작: 예외 발생, BOM 단위 트랜잭션 롤백, boms=FAIL, plans=SKIP
- 스킵 모드(skip)
  - 조건: componentId가 tb_item에 없을 때
  - 동작: 해당 라인만 스킵(미적재 또는 제거), boms=OK, skips 증가

로그 표준
- 스킵: [BOM_SYNC] skip unknown component: bomId=..., lineNo=..., candidate=...

---

## 8) 멱등/트랜잭션

- 멱등 키: 자연키 기반 upsert
- 같은 이벤트 반복 소비 시 결과 불변
- 트랜잭션 경계:
  - 헤더+라인의 원자성 보장(엄격 모드 실패 시 전체 롤백)
  - 스킵 모드에서는 유효 라인만 반영

---

## 9) 로깅/관측성

요약 로그(사이클 종료 시 1줄)
- [BOM_SYNC] hops=[2,2,1] fetched=5 upserts=... deletes=... skips=... newCursor=...

상관관계 ID(권장)
- 요청 헤더: X-Cycle-Id: <UUID>
- ERP(Mock) 로그에 [cycle=...]로 출력하여 사이클별 추적

레벨 권장
- 동작 요약: INFO
- 스킵/경고: WARN
- 실패: ERROR(스택 포함)

---

## 10) 구성 파라미터(상수화)

| Key | 예시 기본값 | 설명 |
|---|---|---|
| erp.base-url | http://localhost:8081/mock | ERP 엔드포인트 |
| sync.intervalSec | 30 | 스케줄 주기(초) |
| sync.boms.max-rows | 2 또는 5 | 페이지 크기(모의) |
| sync.boms.skip-unknown-component | true/false | 스킵 모드 스위치 |
| sync.cursor.epsilon | 1ns/1μs/1ms | 정밀도에 맞게 선택 |
| logging.hop-summary | true | 요약 1줄 출력 여부 |

---

## 11) 회귀 테스트 플레이북(자동화 기준)

- Unknown(strict/skip)
  - given: 존재하지 않는 componentId 주입
  - when: 1사이클
  - then(strict): boms=FAIL, DB 미변경
  - then(skip): boms=OK, skips≥1, 해당 라인 미존재
- Tombstone
  - given: delete-header로 isDeleted=true, updatedAt=now
  - then: deletes≥1, is_deleted=1, 커서 전진
- HOP 경계초
  - given: N건 계단 터치(1초 간격)
  - when: max-rows=2
  - then: got 시퀀스 [2,2,...,마지막,0], fetched=N, newCursor=최대 updatedAt

---

## 12) 예시(요청/응답/커서)

- 요청
- GET /boms?updatedSince=2025-09-07T13:12:09Z



- 응답(요지)
```json
[
  {
    "bomId": "BOM-ITEM-1003|A|STD",
    "itemId": "ITEM-1003",
    "revision": "A",
    "alt": "STD",
    "updatedAt": "2025-09-07T14:46:37.034009401Z",
    "isDeleted": false,
    "lines": [
      { "lineNo": 2, "componentId": "RM-0008", "qty": 0.501, "uom": "EA", "updatedAt": "2025-09-07T14:46:37.034009401Z", "isDeleted": false }
    ]
  }
]
```
- 커서 전진 규칙(내부 HOP)

- nextSince=maxTs+ϵ
- 종료 시 외부 커서 저장: last_synced_at←maxTs

## 13) 에러/코드 표준(요지)
- BOM_COMPONENT_NOT_FOUND: 스킵 모드면 WARN으로 전환, strict면 ERROR로 예외
- FK 위반: items 선반영 필요(우선순위: items → boms)
- 요청 포맷: ‘|’는 URL 인코딩(%7C) 필수
## 14) 변경 이력
v0.9
필터/정렬/커서-ε/Unknown/토므스톤/요약 로그/상관관계 헤더 정의
테스트 플레이북 최소 셋 확정

