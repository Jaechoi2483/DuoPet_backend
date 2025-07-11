package com.petlogue.duopetbackend.adoption.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/adoption")
public class AdoptionSimpleTestController {

    @GetMapping("/simple-test")
    public ResponseEntity<Map<String, String>> simpleTest() {
        log.info("Simple test endpoint called");
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "This is a simple test without any dependencies");
        return ResponseEntity.ok(response);
    }
}