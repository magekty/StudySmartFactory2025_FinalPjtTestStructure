# ADRs(핵심 결정)
1) 시간대: 모든 DATETIME UTC 저장, 화면 로컬표시
2) 키/삭제: 단일 PK/FK(JPA), ON DELETE RESTRICT(보수형), 실삭제는 비즈 로직
3) 코드 일원화: TB_CODE_GROUP/TB_CODE, 참조는 code_id
4) 교대/배치: XOR(설비/작업장) + 부분 유니크(2개)
5) 신원: worker_id = TB_USER.user_id(FK) 단일 신원 원칙
6) 에러 포맷: {code,message,details,traceId,timestamp,path,method}
7) 품질 게이트: db_lint critical=0, major는 화이트리스트(캘린더/배치/email)
