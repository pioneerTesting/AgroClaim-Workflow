package com.pioneer.agro_claim.utils;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DBInfo {
    private String tenantID;
    private String dataSource;

    public DBInfo(String tenantID, String dataSource) {
        this.tenantID = tenantID;
        this.dataSource = dataSource;
    }
}
