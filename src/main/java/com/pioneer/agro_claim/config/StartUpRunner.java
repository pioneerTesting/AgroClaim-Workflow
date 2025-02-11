package com.pioneer.agro_claim.config;

import com.pioneer.agro_claim.utils.DBInfo;
import com.pioneer.agro_claim.utils.DBInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class StartUpRunner implements ApplicationRunner {
    private boolean hasRun = false;

    @Autowired
    private DBInfoService dbInfoService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Set<String> uniqueTenantIds = new HashSet<>();

    @Override
    public void run(ApplicationArguments args) {
        if (!hasRun) {
            initializeDataSourceTenantMap();
            hasRun = true;
        }
    }

    private void initializeDataSourceTenantMap() {
        String sqlQuery = "SELECT mapped_tenant_id, data_source FROM data_source_tenant_map";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sqlQuery);
        for (Map<String, Object> row : result) {
            String tenantId = String.valueOf(row.get("mapped_tenant_id"));
            if (uniqueTenantIds.add(tenantId)) {
                String dataSource = (String) row.get("data_source");
                DBInfo dbInfo = new DBInfo(tenantId, dataSource);
                dbInfoService.save(tenantId, dbInfo);
            }
        }
    }

    @Scheduled(fixedRate = 300000)
    private void updateDataSourceTenantMap() {
        initializeDataSourceTenantMap();
    }
}
