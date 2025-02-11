package com.pioneer.agro_claim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/reassign")
public class ReassignMapController {

    @PostMapping("/map")
    public ResponseEntity<Map<String, Object>> reassignMap(@RequestBody Map<String, String> requestBody){
        System.out.println(requestBody);

        Map<String, Object> returnValue = new HashMap<>();
        returnValue.put("status", "Success");
        returnValue.put("message", "Get Msg");
        return ResponseEntity.ok(returnValue);
    }
}
