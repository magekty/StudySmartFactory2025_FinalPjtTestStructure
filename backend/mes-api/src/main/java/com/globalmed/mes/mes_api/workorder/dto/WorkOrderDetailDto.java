package com.globalmed.mes.mes_api.workorder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WorkOrderDetailDto(
        String workOrderId,
        String workOrderNumber,
        String itemId,
        String processId,
        String equipmentId,
        BigDecimal orderQty,
        BigDecimal producedQty,
        String status, // "P" | "R" | "C"

        // 필요하면 유지: startTs가 이미 잘 나오면 null 유지 가능
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
        OffsetDateTime startTs,

        // created_at / modified_at은 +09:00로 고정 직렬화
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
        OffsetDateTime createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
        OffsetDateTime modifiedAt
) {}