package com.project.RateLimiter.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {
    @GetMapping("/test")
    public String test() {
        log.info("/test endpoint hit");
        return "Hello World";
    }
    @GetMapping("/apiTest")
    public String apiTest() {
        return "{Status : UP}";
    }
    @GetMapping("/ping")
    public String ping() {
        log.info("/ping endpoint hit");
        return "pong";
    }
}
