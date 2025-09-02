// src/main/java/com/globalmed/mes/mes_api/recon/SlackNotifier.java
package com.globalmed.mes.mes_api.recon;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SlackNotifier {
    private final RestClient rc = RestClient.create();
    private final String webhook = System.getenv().getOrDefault("SLACK_WEBHOOK", "");

    public void send(String text) {
        if (webhook == null || webhook.isBlank()) return;
        rc.post().uri(webhook)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"text\":\"" + text.replace("\"","\\\"") + "\"}")
                .retrieve()
                .toBodilessEntity();
    }
}