package com.stockflow.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping({
            "/",
            "/health",
            "/stockflow-backend",
            "/stockflow-backend/",
            "/stockflow-backend/health",
            "/stockflow-backend/actuator/health",
            "/stockflow-backend/actuator/health/liveness",
            "/stockflow-backend/actuator/health/readiness"
    })
    public String health() {
        return "OK";
    }
}
