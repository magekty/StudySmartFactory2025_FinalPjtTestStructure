# Seeds_KeyMap (샘플 키/코드)

| 도메인 | 키 | 샘플 |
|---|---|---|
| EQUIPMENT | E-0001 | STENT_LINE_01 |
| PROCESS | P-0001 | STENT_PROC |
| ITEM | I-0001 | STENT_01 |
| WORK_ORDER | WO-0001 | 데모 지시(발행 후 R→C 시나리오) |

## 코드 시드(발췌)
INSERT INTO TB_CODE_GROUP (group_code, group_name, created_by)
VALUES ('WO_STATUS','작업지시 상태','seed')
ON DUPLICATE KEY UPDATE group_name=VALUES(group_name);

INSERT INTO TB_CODE (group_code, code, name, use_yn, sort_order, created_by)
VALUES ('WO_STATUS','P','Planned','Y',1,'seed'),
       ('WO_STATUS','R','Released','Y',2,'seed'),
       ('WO_STATUS','C','Completed','Y',3,'seed')
ON DUPLICATE KEY UPDATE name=VALUES(name), use_yn=VALUES(use_yn);
