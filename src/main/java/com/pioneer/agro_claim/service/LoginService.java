package com.pioneer.agro_claim.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoginService {

    @Autowired
    private DataSourceService dataSourceService;

    public Map<String, Object> userLogin(Map<String, String> requestBody) throws ParseException{
        Map<String, Object> returnValue = new HashMap<>();
        try {
            JdbcTemplate jdbcTemplate;
            jdbcTemplate = dataSourceService.connectToDatabase("1");

            String userPassQuery = "SELECT id, password FROM agri_claim.user WHERE email = ?";
            Map<String, Object> getUserDetails = new HashMap<>();
            try {
                getUserDetails = jdbcTemplate.queryForMap(userPassQuery, requestBody.get("email").trim());
            } catch (EmptyResultDataAccessException e) {
                getUserDetails = new HashMap<>(); // Handle case where no result is found
            }

            if (requestBody.get("password").trim().equals(getUserDetails.get("password").toString().trim())){
                returnValue.put("status", "Success");
                returnValue.put("message", "Login Successful");
            }else {
                returnValue.put("status", "Failure");
                returnValue.put("message", "Incorrect Password");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return returnValue;
    }

}
