SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS tb_role_menu;
DROP TABLE IF EXISTS tb_menu;
DROP TABLE IF EXISTS tb_user_role;
DROP TABLE IF EXISTS tb_role;
DROP TABLE IF EXISTS tb_user;

DROP TABLE IF EXISTS tb_audit_log;

DROP TABLE IF EXISTS tb_inspection_result;
DROP TABLE IF EXISTS tb_non_conformance;
DROP TABLE IF EXISTS tb_defect;
DROP TABLE IF EXISTS tb_inspection;

DROP TABLE IF EXISTS tb_production_performance;
DROP TABLE IF EXISTS tb_equipment_status_log;

DROP TABLE IF EXISTS tb_shift_assignment;
DROP TABLE IF EXISTS tb_shift_calendar;
DROP TABLE IF EXISTS tb_shift;

DROP TABLE IF EXISTS tb_material_lot;
DROP TABLE IF EXISTS tb_bom;

DROP TABLE IF EXISTS tb_work_order;
DROP TABLE IF EXISTS tb_production_plan;

DROP TABLE IF EXISTS tb_equipment;
DROP TABLE IF EXISTS tb_warehouse;
DROP TABLE IF EXISTS tb_item;
DROP TABLE IF EXISTS tb_process;
DROP TABLE IF EXISTS tb_workcenter;
DROP TABLE IF EXISTS tb_workshop;

DROP TABLE IF EXISTS tb_kpi_target;

DROP TABLE IF EXISTS tb_code;
DROP TABLE IF EXISTS tb_code_group;

SET FOREIGN_KEY_CHECKS = 1;
