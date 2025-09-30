package com.globalmed.mes.mes_api.equipstatus.dto;

import com.globalmed.mes.mes_api.code.dto.CertDto;
import com.globalmed.mes.mes_api.code.dto.ProcessDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
public record EquipDetailDto(
        // 설비 아이디
        String equipmentId,
        // 설비 명
        String equipmentName,
        // 워크센터 네임
        String workcenterName,
        // 상태 이름(RUN)
        String statusCode,
        // 만들어진 시간(점검, 기계 수명 계산옹)
        OffsetDateTime createAt,
        // 작업지시(있으면)
        String workOrederName,
        // 만드는 아이템(있으면)
        String itemName,
        // 제작 수량(있으면)
        BigDecimal produceQty,
        // 목표 수량(있으면)
        BigDecimal orderQty,
        // 현재 작업자 목록(여러명일 수 있음)(workerName{이름},workerNumber{사원번호})
        List<EWorkerDto> workers,
        // 현재 시프트(교대) 시간(주간,전반야,후반야)
        String shiftName,
        // 현재 시프트 시작시간
        LocalDateTime shiftStartTs,
        // 현재 시프트 종료 시간
        LocalDateTime shiftEndTs,
        // 해당 설비를 가동하는데 필요한 자격증 들
        List<CertDto> requiredCerts,
        // 설비에서 연결된 공정 목록(processId, processName, Description)
        List<ProcessDto> equipProcess

) {
    public record EWorkerDto(
            String workerName,  // 작업자 이름
            String workerNumber // 작업자 사원 번호
    ) {}
}
