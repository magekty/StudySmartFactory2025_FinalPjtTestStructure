// src/main/java/com/globalmed/mes/mes_api/internal/config/ShadowConfigController.java
package com.globalmed.mes.mes_api.internal.config;

import com.globalmed.mes.mes_api.config.ShadowProps;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/config")
public class ShadowConfigController {
    private final ShadowProps props;
    public ShadowConfigController(ShadowProps props){ this.props = props; }

    public record ShadowView(
            boolean enabled, boolean workOrders, boolean performances, boolean backflush, boolean cost
    ) {}

    @GetMapping("/shadow")
    public ResponseEntity<ShadowView> get() {
        return ResponseEntity.ok(new ShadowView(
                props.enabled(), props.workOrders(), props.performances(), props.backflush(), props.cost()
        ));
    }
}