package com.globalmed.mes.mes_api.kpi;
import com.globalmed.mes.mes_api.kpi.service.DailyKpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
@Service
@RequiredArgsConstructor
public class KpiBatchScheduler {

    private final DailyKpiService dailyKpiService;
    /**
     * 매일 새벽 1시(KTC기준 10시)에 배치(일일 총 생산량) KPI 계산 및 저장
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyBatchKpi() {
        LocalDate yesterdayUtc = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        dailyKpiService.runDailyBatchKpiCalculation(yesterdayUtc);
    }
}
