package com.pioneer.agro_claim.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.ParseException;
import java.util.*;

@Service
public class UploadImageService {

    @Autowired
    private DataSourceService dataSourceService;

    private static final String UPLOAD_IMG_DIR = "D:/uploaded_images/"; // Change this as needed
    private static final String UPLOAD_DOC_DIR = "D:/uploaded_documents/"; // Change this as needed

    public Map<String, Object> saveImage(Map<String, Object> requestBody) throws ParseException {
        Map<String, Object> returnValue = new HashMap<>();
        try {
            JdbcTemplate jdbcTemplate;
            jdbcTemplate = dataSourceService.connectToDatabase("1");

            String authName = (String) requestBody.get("authenticateUsername");
            String userNameQuery = "SELECT id FROM agri_claim.user WHERE email = ?";
            Map<String, Object> getUserId = new HashMap<>();

            try {
                getUserId = jdbcTemplate.queryForMap(userNameQuery, authName);
            } catch (EmptyResultDataAccessException e) {
                getUserId = new HashMap<>(); // Handle case where no result is found
            }

            // Create user-specific directory
            String userUploadDir = UPLOAD_IMG_DIR + getUserId.get("id") + "/";
            File directory = new File(userUploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            String userId = getUserId.get("id").toString();
            List<Map<String, Object>> files = (List<Map<String, Object>>) requestBody.get("images");
            if (files == null || files.isEmpty()) {
                returnValue.put("status", "Failure");
                returnValue.put("error", "No files found in request.");
                return returnValue;
            }
            List<Map<String, Object>> savedImageFiles = new ArrayList<>();

            for (Map<String, Object> fileData : files) {
                String fileName = (String) fileData.get("fileName");
                String[] parts = fileName.split("\\.");
                String extension = parts[parts.length - 1];
                String base64Data = (String) fileData.get("base64Data");
                if (fileName == null || base64Data == null || base64Data.isEmpty()) {
                    continue; // Skip invalid entries
                }
                // Decode Base64 and save file
                byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
                String filePath = userUploadDir + fileName;

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(decodedBytes);
                }

                // Insert into database and retrieve generated ID
                String addUserImageProofQuery = "INSERT INTO agri_claim.image_proof (image_path, extension, name) VALUES (?, ?, ?)";
                KeyHolder keyHolder = new GeneratedKeyHolder();

                int updateFlag = jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(addUserImageProofQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, filePath);
                    ps.setString(2, extension);
                    ps.setString(3, fileName);
                    return ps;
                }, keyHolder);

                if (updateFlag > 0) {
                    Number generatedId = keyHolder.getKey();
                    Map<String, Object> fileResponse = new HashMap<>();
                    fileResponse.put("filePath", filePath);
                    fileResponse.put("insertedId", generatedId);
                    savedImageFiles.add(fileResponse);
                }
            }
            returnValue.put("status", "Success");
            returnValue.put("message", "Images saved successfully.");
            returnValue.put("savedImageFiles", savedImageFiles);

            // Create user-specific Doc directory
            String userDocUploadDir = UPLOAD_DOC_DIR + getUserId.get("id") + "/";
            File docDirectory = new File(userDocUploadDir);
            if (!docDirectory.exists()) {
                docDirectory.mkdirs();
            }

            List<Map<String, Object>> docFiles = (List<Map<String, Object>>) requestBody.get("documents");
            if (files == null || files.isEmpty()) {
                returnValue.put("status", "Failure");
                returnValue.put("error", "No files found in request.");
                return returnValue;
            }
            List<Map<String, Object>> savedDocFiles = new ArrayList<>();

            for (Map<String, Object> docFile : docFiles){
                String fileName = (String) docFile.get("fileName");
                String[] parts = fileName.split("\\.");
                String extension = parts[parts.length - 1];
                String base64Data = (String) docFile.get("base64Data");
                if (fileName == null || base64Data == null || base64Data.isEmpty()) {
                    continue; // Skip invalid entries
                }
                // Decode Base64 and save file
                byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
                String filePath = userDocUploadDir + fileName;

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(decodedBytes);
                }

                // Insert into database and retrieve generated ID
                String addUserImageProofQuery = "insert into agri_claim.document (document_name, extension, path, uploaded_time) VALUES (?, ?, ?, now())";
                KeyHolder keyHolder = new GeneratedKeyHolder();

                int updateFlag = jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(addUserImageProofQuery, Statement.RETURN_GENERATED_KEYS);
                    ps.setString(1, fileName);
                    ps.setString(2, extension);
                    ps.setString(3, filePath);
                    return ps;
                }, keyHolder);

                if (updateFlag > 0) {
                    Number generatedId = keyHolder.getKey();
                    Map<String, Object> fileResponse = new HashMap<>();
                    fileResponse.put("filePath", filePath);
                    fileResponse.put("insertedId", generatedId);
                    savedDocFiles.add(fileResponse);
                }
            }
            returnValue.put("savedDocFiles", savedDocFiles);

            String getMaxIdQuery = "select max(id) as 'max' from agri_claim.damage";
            Map<String, Object> getMaxId = new HashMap<>();
            try {
                getMaxId = jdbcTemplate.queryForMap(getMaxIdQuery);
            } catch (EmptyResultDataAccessException e) {
                getMaxId = new HashMap<>(); // Handle case where no result is found
            }
            String autogeneratedId = "CLAIM-00"+getMaxId.get("max");

            // Insert into database and retrieve generated ID
            String addUserImageProofQuery = "insert into agri_claim.damage (claim_id, claim_status_id, user_id, owner_id, status, claim_time) " +
                    "VALUES (?, '1', ?, null, true, now())";
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int updateFlag = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(addUserImageProofQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, autogeneratedId);
                ps.setString(2, userId);
                return ps;
            }, keyHolder);

            String damageId;
            if (updateFlag > 0) {
                Number generatedId = keyHolder.getKey();
                damageId = generatedId.toString();
            } else {
                damageId = "";
            }

            // Extract damageLocation
            List<List<List<Map<String, Object>>>> damageLocations =
                    (List<List<List<Map<String, Object>>>>) requestBody.get("damageLocation");

            if (damageLocations != null) {
                int count = 1;
                for (List<List<Map<String, Object>>> outerList : damageLocations) {
                    for (List<Map<String, Object>> innerList : outerList) {
                        for (Map<String, Object> coordinates : innerList) {
                            String lat = coordinates.get("lat").toString();
                            String lng = coordinates.get("lng").toString();
                            String addDamageLocationQuery = "insert into agri_claim.damage_location " +
                                    "(lat, lng, damage_id, loc_no) " +
                                    "VALUES (?, ?, ?, ?)";
                            KeyHolder damageLocationKeyHolder = new GeneratedKeyHolder();


                            int finalCount = count;
                            int damageLocationUpdateFlag = jdbcTemplate.update(connection -> {
                                PreparedStatement ps = connection.prepareStatement(addDamageLocationQuery, Statement.RETURN_GENERATED_KEYS);
                                ps.setString(1, lat);
                                ps.setString(2, lng);
                                ps.setString(3, damageId);
                                ps.setString(4, String.valueOf(finalCount));
                                return ps;
                            }, damageLocationKeyHolder);

                            if (damageLocationUpdateFlag <= 0){
                                returnValue.put("status", "Failure");
                                returnValue.put("error", "failed to insert location");
                                break;
                            }
                        }
                        count++;
                    }
                }
            }

            returnValue.forEach((key, value) -> {
                System.out.println("Key: " + key + ", Value: " + value);
            });

            if (returnValue.containsKey("savedImageFiles")) {
                List<Map<String, Object>> savedImageFilesList = (List<Map<String, Object>>) returnValue.get("savedImageFiles");

                System.out.println("Saved Image Files:");
                for (Map<String, Object> imageFile : savedImageFilesList) {
                    System.out.println("File Path: " + imageFile.get("filePath"));
                    System.out.println("Inserted ID: " + imageFile.get("insertedId"));

                    String addImageMapQuery = "insert into agri_claim.image_map " +
                            "(image_id, damage_id) " +
                            "VALUES (?, ?)";
                    KeyHolder damageLocationKeyHolder = new GeneratedKeyHolder();


                    int damageLocationUpdateFlag = jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(addImageMapQuery, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, imageFile.get("insertedId").toString());
                        ps.setString(2, damageId);
                        return ps;
                    }, damageLocationKeyHolder);

                    if (damageLocationUpdateFlag <= 0){
                        returnValue.put("status", "Failure");
                        returnValue.put("error", "failed to add image");
                        break;
                    }

                }
            }

            if (returnValue.containsKey("savedDocFiles")) {
                List<Map<String, Object>> savedDocFilesList = (List<Map<String, Object>>) returnValue.get("savedDocFiles");

                System.out.println("Saved Document Files:");
                for (Map<String, Object> docFile : savedDocFilesList) {
                    System.out.println("File Path: " + docFile.get("filePath"));
                    System.out.println("Inserted ID: " + docFile.get("insertedId"));

                    String addDocumentMapQuery = "insert into agri_claim.document_map " +
                            "(document_id, damage_id) " +
                            "VALUES (?, ?)";
                    KeyHolder damageLocationKeyHolder = new GeneratedKeyHolder();


                    int damageLocationUpdateFlag = jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(addDocumentMapQuery, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, docFile.get("insertedId").toString());
                        ps.setString(2, damageId);
                        return ps;
                    }, damageLocationKeyHolder);

                    if (damageLocationUpdateFlag <= 0){
                        returnValue.put("status", "Failure");
                        returnValue.put("error", "failed to add image");
                        break;
                    }
                }
            }


            return returnValue;
        } catch (Exception e) {
            returnValue.put("status", "Failure");
            returnValue.put("error", "Failed to save some images: " + e.getMessage());
            return returnValue;
        }
    }
}
