// src/main/java/com/globalmed/mes/mes_api/recon/DailyReconciliationJob.java
package com.globalmed.mes.mes_api.recon;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DailyReconciliationJob {

    private final DailyReconciliationService svc;
    private final SlackNotifier slack;

    @Value("${recon.zone:UTC}")
    private String zone;

    // 02:00 UTC (application.yml의 recon.cron/recon.zone로 제어)
    @Scheduled(cron = "${recon.cron:0 0 2 * * *}", zone = "${recon.zone:UTC}")
    public void runDaily() {
        LocalDate kstYesterday = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);
        Map<String, Map<String, String>> res = svc.runForKst(kstYesterday);
        slack.send(String.format("[대사] %s KST 결과\nWO: %s\nPERF: %s\nBACKFLUSH: %s",
                kstYesterday, res.get("WO"), res.get("PERF"), res.get("BACKFLUSH")));
    }
}