package com.pioneer.agro_claim.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class SaveUserService {

    @Autowired
    private DataSourceService dataSourceService;

    public Map<String, Object> saveUser(Map<String, String> requestBody) throws ParseException{
        Map<String, Object> returnValue = new HashMap<>();
        try {
            JdbcTemplate jdbcTemplate;
            jdbcTemplate = dataSourceService.connectToDatabase("1");

            String userNameQuery = "SELECT name FROM agri_claim.user WHERE email = ?";
            Map<String, Object> getUserName = new HashMap<>();

            try {
                getUserName = jdbcTemplate.queryForMap(userNameQuery, requestBody.get("email").trim());
            } catch (EmptyResultDataAccessException e) {
                getUserName = new HashMap<>(); // Handle case where no result is found
            }

            System.out.println(getUserName);

            if (!getUserName.isEmpty()){
                returnValue.put("status", "Failure");
                returnValue.put("message", "User Already exist");
                return returnValue;
            }

            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String encryptedPassword = passwordEncoder.encode(requestBody.get("password").trim());
//            String encryptedPassword = (requestBody.get("password").trim());


            String addUserQuery = "INSERT INTO user (address, dob, gender, name, phone_no, roleId, email, aadharCardNo, bankName, bankAccountNo, ifscCode, active, block, password) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            int updateFlag = jdbcTemplate.update(addUserQuery,
                    requestBody.get("address").trim(),
                    requestBody.get("dob").trim(),
                    requestBody.get("gender").trim(),
                    requestBody.get("name").trim(),
                    requestBody.get("phoneNo").trim(),
                    1,
                    requestBody.get("email").trim(),
                    requestBody.get("aadharCardNo").trim(),
                    requestBody.get("bankName").trim(),
                    requestBody.get("bankAccountNo").trim(),
                    requestBody.get("ifscCode").trim(),
                    true,
                    true,
                    encryptedPassword
            );

            if (updateFlag > 0) {
                returnValue.put("status", "Success");
                returnValue.put("message", "User saved successfully");
            } else {
                returnValue.put("status", "Failure");
                returnValue.put("message", "User not saved");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /*returnValue.put("status", "Failure");
        returnValue.put("message", "Internal Server Error: ");*/
        return returnValue;
    }

    private Map<String, Object> queryForMap(JdbcTemplate jdbcTemplate, String query) {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(query);
            if (result.size() >= 1) {
                return result.get(0);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
