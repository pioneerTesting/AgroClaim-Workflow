package com.pioneer.agro_claim.service;

import com.pioneer.agro_claim.advice.LoggingAdvice;
import com.pioneer.agro_claim.utils.DBInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DataSourceService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DBInfoService dbInfoService;

    Logger logger = LoggerFactory.getLogger(LoggingAdvice.class);

    public Map<String, String> getDataSource(String tenantId) {
        if (dbInfoService.hasKey(tenantId)) {
            return parseJdbcUrl(dbInfoService.get(tenantId));
        }
        return null;
    }

    public List<Map<String, String>> getAllDataSources() {
        List<Map<String, String>> allDataSources = new ArrayList<>();
        Map<String, String> allTenantsData = dbInfoService.getAllDataSources();
        allTenantsData.remove("1");
        allTenantsData.remove("2");
        for (Map.Entry<String, String> entry : allTenantsData.entrySet()) {
            Map<String, String> parsedUrl;
            if (dbInfoService.hasKey(entry.getKey())) {
                parsedUrl = parseJdbcUrl(entry.getValue());
                parsedUrl.put("tenantId", entry.getKey());
                allDataSources.add(parsedUrl);
            }
        }

        return allDataSources;
    }

    public JdbcTemplate connectToDatabase(String tenant) throws Exception {
        try {
            String jdbcUrl = dbInfoService.get(tenant);
            Map<String, String> result = parseJdbcUrl(jdbcUrl);
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl(result.get("dbUrl"));
            dataSource.setUsername(result.get("dbUsername"));
            dataSource.setPassword(result.get("dbPassword"));
            jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate;
        }
        catch (Exception e){
            logger.error(e.toString());
            throw new Exception("Error while connecting to database");
        }
    }

    public static Map<String, String> parseJdbcUrl(String jdbcUrl) {
        HashMap<String, String> result = new HashMap<>();
        String url = null;
        String username = null;
        String password = null;
        Pattern pattern = Pattern.compile("jdbc:(.*?):\\/\\/([^:/]+):(\\d+)/(.*?)\\?(.*)");
        Matcher matcher = pattern.matcher(jdbcUrl);
        if (matcher.find()) {
            url = "jdbc:" + matcher.group(1) + "://" + matcher.group(2) + ":" + matcher.group(3) + "/" + matcher.group(4);
            String[] params = matcher.group(5).split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if ("user".equalsIgnoreCase(keyValue[0])) {
                        username = keyValue[1];
                    } else if ("password".equalsIgnoreCase(keyValue[0])) {
                        password = keyValue[1];
                    }
                }
            }
        }

        result.put("dbUrl", url);
        result.put("dbUsername", username);
        result.put("dbPassword", password);

        return result;
    }
}
