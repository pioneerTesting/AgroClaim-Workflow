package com.pioneer.agro_claim.controller;

import com.pioneer.agro_claim.service.UploadImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private UploadImageService uploadImageService;

    private static final String UPLOAD_DIR = "C:/uploaded_images/"; // Change this as needed

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> saveImage(@RequestBody Map<String, Object> requestBody){
        try {
            Map<String, Object> jsonData = uploadImageService.saveImage(requestBody);
            return (ResponseEntity.ok(jsonData)) ;
        } catch (Exception e) {
            Map<String, Object> returnValue = new HashMap<>();
            returnValue.put("status", "Failure");
            returnValue.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnValue);
        }
    }

    @PostMapping("/damages")
    public ResponseEntity<Map<String, Object>> saveClaim(@RequestBody Map<String, Object> requestBody){
        try {
            Map<String, Object> jsonData = uploadImageService.saveImage(requestBody);

            return (ResponseEntity.ok(jsonData)) ;
        } catch (Exception e) {
            Map<String, Object> returnValue = new HashMap<>();
            returnValue.put("status", "Failure");
            returnValue.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(returnValue);
        }
    }
}
