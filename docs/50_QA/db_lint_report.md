# DB Lint Report (자리표시)
- 마지막 실행 결과 요약 붙여넣기

    # DB Lint Report

    - Generated: 2025-08-09 04:38:27 UTC

    ## EXPLAIN (KPI 최근 7일)
    ```
    {'id': 1, 'select_type': 'SIMPLE', 'table': 'TB_KPI_TARGET', 'partitions': None, 'type': 'range', 'possible_keys': 'uk_kpi_target_date_eqp_proc_item', 'key': 'uk_kpi_target_date_eqp_proc_item', 'key_len': '3', 'ref': None, 'rows': 1, 'filtered': 100.0, 'Extra': 'Using index condition'}
    ```

    ## Summary

    - critical: 0
    - major: 0
    - minor: 0

    ## Details
