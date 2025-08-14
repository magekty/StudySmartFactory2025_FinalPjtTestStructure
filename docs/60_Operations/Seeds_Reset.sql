-- Seeds & Reset (샘플/발췌)
INSERT INTO TB_CODE_GROUP (group_code, group_name, created_by)
VALUES ('WO_STATUS','작업지시 상태','seed')
ON DUPLICATE KEY UPDATE group_name=VALUES(group_name);

INSERT INTO TB_CODE (group_code, code, name, use_yn, sort_order, created_by)
VALUES ('WO_STATUS','P','Planned','Y',1,'seed'),
       ('WO_STATUS','R','Released','Y',2,'seed'),
       ('WO_STATUS','C','Completed','Y',3,'seed')
ON DUPLICATE KEY UPDATE name=VALUES(name), use_yn=VALUES(use_yn);

-- 역할/사용자(비번 해시 교체 필요)
INSERT INTO TB_ROLE (role_code, role_name, created_by)
VALUES ('ROLE_OP','운영자','seed')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

INSERT INTO TB_USER (user_id, username, password_hash, password_algo, is_active, created_by)
VALUES ('00000000-0000-0000-0000-0000000000OP','op','$2a$10$<bcrypt>','bcrypt',1,'seed')
ON DUPLICATE KEY UPDATE is_active=1;
