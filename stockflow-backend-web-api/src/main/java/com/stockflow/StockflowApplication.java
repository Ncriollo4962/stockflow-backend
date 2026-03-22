package com.stockflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class StockflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockflowApplication.class, args);
    }

    @GetMapping("/")
    public String healthcheckRoot() {
        return "OK";
    }
}
