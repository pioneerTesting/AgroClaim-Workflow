package com.pioneer.agro_claim.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private DataSourceService dataSourceService;

    public Map<String, Object> getUserDetails(Map<String, String> requestBody) throws ParseException{
        Map<String, Object> returnValue = new HashMap<>();
        try {
            JdbcTemplate jdbcTemplate;
            jdbcTemplate = dataSourceService.connectToDatabase("1");

            String userDetailsQuery = "SELECT name, gender, dob, address FROM agri_claim.user where email = ?";
            Map<String, Object> userDetailsMap = new HashMap<>();
            try {
                userDetailsMap = jdbcTemplate.queryForMap(userDetailsQuery, requestBody.get("email").trim());
            } catch (EmptyResultDataAccessException e) {
                userDetailsMap = new HashMap<>(); // Handle case where no result is found
            }

            System.out.println(userDetailsMap);

            if (!userDetailsMap.isEmpty()){
                returnValue.put("status", "Success");
                returnValue.put("name", userDetailsMap.get("name"));
                returnValue.put("gender", userDetailsMap.get("gender"));
                returnValue.put("dob", userDetailsMap.get("dob"));
                returnValue.put("address", userDetailsMap.get("address"));
            }else {
                returnValue.put("status", "Failure");
                returnValue.put("message", "User Not Found");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return returnValue;
    }
}
