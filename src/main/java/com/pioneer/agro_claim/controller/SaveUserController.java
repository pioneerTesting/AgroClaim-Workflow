package com.pioneer.agro_claim.controller;

import com.pioneer.agro_claim.service.LoginService;
import com.pioneer.agro_claim.service.SaveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class SaveUserController {

    @Autowired
    private SaveUserService saveUserService;

    @Autowired
    private LoginService loginService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> userSignup(@RequestBody Map<String, String> requestBody){
        try{
            System.out.println(requestBody);
            Map<String, Object> jsonData = saveUserService.saveUser(requestBody);
            return ResponseEntity.ok(jsonData);

        } catch (Exception e) {
            Map<String, Object> returnValue = new HashMap<>();
            returnValue.put("status", "Failure");
            returnValue.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnValue);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> userSignIn(@RequestBody Map<String, String> requestBody){
        try {
            Map<String, Object> jsonData = loginService.userLogin(requestBody);
            return ResponseEntity.ok(jsonData);
        } catch (Exception e) {
            Map<String, Object> returnValue = new HashMap<>();
            returnValue.put("status", "Failure");
            returnValue.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnValue);
        }

    }

}
