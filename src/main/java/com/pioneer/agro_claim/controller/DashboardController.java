package com.pioneer.agro_claim.controller;

import com.pioneer.agro_claim.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @PostMapping("/getuserdata")
    public ResponseEntity<Map<String, Object>> getUserDetails(@RequestBody Map<String, String> requestBody){
        try{
            Map<String, Object> jsonData = dashboardService.getUserDetails(requestBody);
            return ResponseEntity.ok(jsonData);
        } catch (Exception e) {
            Map<String, Object> returnValue = new HashMap<>();
            returnValue.put("status", "Failure");
            returnValue.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnValue);
        }
    }
}
