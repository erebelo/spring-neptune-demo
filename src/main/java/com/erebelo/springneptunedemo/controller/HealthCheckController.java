package com.erebelo.springneptunedemo.controller;

import static com.erebelo.springneptunedemo.constant.BusinessConstant.HEALTH_CHECK_PATH;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(HEALTH_CHECK_PATH)
public class HealthCheckController {

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getHealthCheck() {
        log.info("Getting health check");
        return ResponseEntity.ok("Spring Neptune Demo application is up and running");
    }
}
