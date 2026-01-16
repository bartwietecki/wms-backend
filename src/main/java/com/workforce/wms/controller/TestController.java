package com.workforce.wms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestController {

    @GetMapping("/api/ping")
    String ping() {
        return "pong";
    }
}
