package com.whoiszxl.datasync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 同步配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "sync")
public class SyncProperties {

    /**
     * 批处理大小
     */
    private int batchSize = 1000;

    /**
     * 启动时是否自动同步
     */
    private boolean autoSync = false;

    /**
     * 表配置信息
     */
    private Map<String, TableConfig> tables = new HashMap<>();

    /**
     * 表配置类
     */
    @Data
    public static class TableConfig {
        /**
         * 源表名
         */
        private String sourceTable;
        
        /**
         * 目标表名
         */
        private String targetTable;
        
        /**
         * 主键字段
         */
        private String idField;
        
        /**
         * 字段映射关系（源字段 -> 目标字段）
         */
        private Map<String, String> mapping = new HashMap<>();
    }
} 