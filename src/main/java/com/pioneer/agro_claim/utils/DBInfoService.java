package com.pioneer.agro_claim.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class DBInfoService {
    private final Map<String, DBInfo> DBMap=new ConcurrentHashMap<>();

    public boolean save(String tenantId, DBInfo dbInfo) {
        DBMap.put(tenantId, dbInfo);
        return true;
    }

    public String get(String tenantID) {
        return String.valueOf(DBMap.get(tenantID).getDataSource());
    }

    public boolean hasKey(String tenantID){
        return DBMap.containsKey(tenantID);
    }

    public Map<String, String> getAllDataSources() {
        return DBMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getDataSource()));
    }
}
