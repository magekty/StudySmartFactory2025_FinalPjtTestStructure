CREATE TABLE IF NOT EXISTS if_plan_queue (
    queue_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id CHAR(36) NOT NULL COMMENT '변경 항목의 고유 ID (ERP tb_production_plan.plan_id)',
    change_type ENUM('C', 'U', 'D') NOT NULL COMMENT 'C: Create, U: Update, D: Delete',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING' COMMENT '처리 상태',
    retry_count INT DEFAULT 0 COMMENT '재시도 횟수',
    KEY idx_queue_status (status, created_at)
) ENGINE=InnoDB;

-- UPDATE 트리거: 상태 변경 (CONFIRMED -> PENDING 전송), 핵심 필드 변경, 소프트 삭제 감지
-- 1. 종결자(DELIMITER)를 임시로 $$로 변경합니다.
--    (BEGIN...END 블록 내의 세미콜론이 오류를 일으키지 않도록 합니다.)
DELIMITER $$

CREATE TRIGGER trg_plan_after_update
AFTER UPDATE ON tb_production_plan
FOR EACH ROW
BEGIN
    -- 1. 신규 CONFIRMED 상태 감지 (ERP -> MES 초기 전송 조건)
    -- DRAFT 등 이전 상태에서 CONFIRMED로 변경될 때 'C' 이벤트로 전송
    IF OLD.status <> 'CONFIRMED' AND NEW.status = 'CONFIRMED' THEN
        INSERT INTO if_plan_queue (plan_id, change_type)
        VALUES (NEW.plan_id, 'C'); -- 'C' (Create/New Plan)

    -- 2. 핵심 필드 UPDATE 감지 (Confirmed 상태에서 계획 변경 시)
    -- QTY, START_DATE, END_DATE 중 하나라도 바뀌었을 때 'U' 이벤트로 전송
    ELSEIF NEW.status = 'CONFIRMED' AND (
        OLD.qty <> NEW.qty OR
        OLD.start_date <> NEW.start_date OR
        OLD.end_date <> NEW.end_date
    ) THEN
        INSERT INTO if_plan_queue (plan_id, change_type)
        VALUES (NEW.plan_id, 'U'); -- 'U' (Update/Change Plan)

    -- 3. 소프트 삭제 감지 (DRAFT/CONFIRMED 상태의 계획이 is_deleted=1로 변경될 때)
    ELSEIF OLD.is_deleted = 0 AND NEW.is_deleted = 1 THEN
        -- MES에서도 이 계획을 취소(CANCELED) 처리하도록 'D' 이벤트 전송
        INSERT INTO if_plan_queue (plan_id, change_type)
        VALUES (NEW.plan_id, 'D'); -- 'D' (Delete/Cancel Plan)
    END IF;
END$$

-- 2. 종결자(DELIMITER)를 다시 세미콜론(;)으로 복구합니다.
DELIMITER ;