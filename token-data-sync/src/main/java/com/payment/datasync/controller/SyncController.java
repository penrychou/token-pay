package com.payment.datasync.controller;

import com.payment.datasync.service.DataSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据同步控制器
 */
@Slf4j
@RestController
@RequestMapping("/sync")
public class SyncController {

    @Autowired
    private DataSyncService dataSyncService;

    /**
     * 同步所有配置的表
     *
     * @return 同步结果
     */
    @PostMapping("/all")
    public Map<String, Object> syncAllTables() {
        log.info("开始同步所有表");
        Map<String, DataSyncService.SyncResult> results = dataSyncService.syncAllTables();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "同步完成");
        response.put("results", results);
        
        return response;
    }

    /**
     * 同步指定表
     *
     * @param tableName 表名
     * @return 同步结果
     */
    @PostMapping("/{tableName}")
    public Map<String, Object> syncTable(@PathVariable String tableName) {
        log.info("开始同步表: {}", tableName);
        DataSyncService.SyncResult result = dataSyncService.syncTable(tableName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.getException() == null);
        response.put("message", result.getException() == null ? "同步完成" : "同步失败: " + result.getException().getMessage());
        response.put("result", result);
        
        return response;
    }

    /**
     * 获取同步状态信息
     *
     * @return 同步配置信息
     */
    @GetMapping("/status")
    public Map<String, Object> getSyncStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("success", true);
        status.put("message", "获取状态成功");
        status.put("availableTables", dataSyncService.getClass().getSimpleName());
        
        return status;
    }
} 