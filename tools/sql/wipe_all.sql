SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE tb_role_menu;
TRUNCATE TABLE tb_menu;
TRUNCATE TABLE tb_user_role;
TRUNCATE TABLE tb_role;
TRUNCATE TABLE tb_user;

TRUNCATE TABLE tb_audit_log;

TRUNCATE TABLE tb_inspection_result;
TRUNCATE TABLE tb_non_conformance;
TRUNCATE TABLE tb_defect;
TRUNCATE TABLE tb_inspection;

TRUNCATE TABLE tb_production_performance;
TRUNCATE TABLE tb_equipment_status_log;

TRUNCATE TABLE tb_shift_assignment;
TRUNCATE TABLE tb_shift_calendar;
TRUNCATE TABLE tb_shift;

TRUNCATE TABLE tb_material_lot;
TRUNCATE TABLE tb_bom;

TRUNCATE TABLE tb_work_order;
TRUNCATE TABLE tb_production_plan;

TRUNCATE TABLE tb_equipment;
TRUNCATE TABLE tb_warehouse;
TRUNCATE TABLE tb_item;
TRUNCATE TABLE tb_process;
TRUNCATE TABLE tb_workcenter;
TRUNCATE TABLE tb_workshop;

TRUNCATE TABLE tb_kpi_target;

TRUNCATE TABLE tb_code;
TRUNCATE TABLE tb_code_group;

SET FOREIGN_KEY_CHECKS = 1;
