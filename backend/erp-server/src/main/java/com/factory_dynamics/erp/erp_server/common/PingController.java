package com.factory_dynamics.erp.erp_server.common;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {
    @Operation(summary = "ping")
    @GetMapping("/ping")
    public String ping() { return "pong"; }
}