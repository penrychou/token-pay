package com.payment.datasync.service;

import com.payment.datasync.config.SyncProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据同步服务
 */
@Slf4j
@Service
public class DataSyncService {

    @Autowired
    @Qualifier("sourceJdbcTemplate")
    private JdbcTemplate sourceJdbcTemplate;

    @Autowired
    @Qualifier("targetJdbcTemplate")
    private JdbcTemplate targetJdbcTemplate;

    @Autowired
    private SyncProperties syncProperties;

    /**
     * 同步指定表的数据
     *
     * @param tableName 表名
     * @return 同步结果
     */
    @Transactional(rollbackFor = Exception.class)
    public SyncResult syncTable(String tableName) {
        SyncProperties.TableConfig tableConfig = syncProperties.getTables().get(tableName);
        if (tableConfig == null) {
            throw new RuntimeException("未找到表 " + tableName + " 的同步配置");
        }

        log.info("开始同步表 {}", tableName);
        SyncResult result = new SyncResult(tableName);

        try {
            // 构建查询SQL
            String sourceFields = String.join(", ", tableConfig.getMapping().keySet());
            String sourceTable = tableConfig.getSourceTable();
            String idField = tableConfig.getIdField();

            String sql = "SELECT " + sourceFields + " FROM " + sourceTable;
            log.info("查询源表的SQL: {}", sql);

            // 查询源表数据
            List<Map<String, Object>> sourceData = sourceJdbcTemplate.queryForList(sql);
            log.info("源表数据条数: {}", sourceData.size());

            // 如果源表为空，则结束
            if (sourceData.isEmpty()) {
                log.info("源表 {} 没有数据，无需同步", tableName);
                return result;
            }

            // 分批处理
            int batchSize = syncProperties.getBatchSize();
            int totalSize = sourceData.size();
            int totalBatches = (totalSize + batchSize - 1) / batchSize;

            for (int i = 0; i < totalBatches; i++) {
                int fromIndex = i * batchSize;
                int toIndex = Math.min((i + 1) * batchSize, totalSize);
                List<Map<String, Object>> batchData = sourceData.subList(fromIndex, toIndex);

                processBatch(tableConfig, batchData, result);
                log.info("完成批次 {}/{} 的处理，大小: {}", i + 1, totalBatches, batchData.size());
            }

            log.info("表 {} 同步完成，插入: {}，更新: {}，失败: {}", 
                    tableName, result.getInserted(), result.getUpdated(), result.getFailed());
            return result;
        } catch (Exception e) {
            log.error("同步表 " + tableName + " 时出错", e);
            result.setException(e);
            throw e;
        }
    }

    /**
     * 处理一批数据
     *
     * @param tableConfig 表配置
     * @param batchData   批数据
     * @param result      同步结果
     */
    private void processBatch(SyncProperties.TableConfig tableConfig, List<Map<String, Object>> batchData, SyncResult result) {
        String targetTable = tableConfig.getTargetTable();
        Map<String, String> mapping = tableConfig.getMapping();
        String idField = tableConfig.getIdField();

        // 获取所有ID
        List<Object> ids = batchData.stream()
                .map(m -> m.get(idField))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            log.warn("批次中没有有效的ID，跳过处理");
            return;
        }

        // 查询目标库中已存在的ID
        String inClause = String.join(",", Collections.nCopies(ids.size(), "?"));
        String targetIdField = mapping.get(idField);
        String existsSql = "SELECT " + targetIdField + " FROM " + targetTable + " WHERE " + targetIdField + " IN (" + inClause + ")";
        
        List<Object> existingIds = targetJdbcTemplate.queryForList(existsSql, ids.toArray(), Object.class);
        Set<Object> existingIdSet = new HashSet<>(existingIds);

        // 将数据分为要插入和要更新的
        List<Map<String, Object>> toInsert = new ArrayList<>();
        List<Map<String, Object>> toUpdate = new ArrayList<>();

        for (Map<String, Object> sourceRow : batchData) {
            Object id = sourceRow.get(idField);
            if (id == null) continue;

            if (existingIdSet.contains(id)) {
                toUpdate.add(sourceRow);
            } else {
                toInsert.add(sourceRow);
            }
        }

        // 执行插入
        if (!toInsert.isEmpty()) {
            try {
                executeInserts(tableConfig, toInsert);
                result.addInserted(toInsert.size());
            } catch (Exception e) {
                log.error("执行插入操作失败", e);
                result.addFailed(toInsert.size());
            }
        }

        // 执行更新
        if (!toUpdate.isEmpty()) {
            try {
                executeUpdates(tableConfig, toUpdate);
                result.addUpdated(toUpdate.size());
            } catch (Exception e) {
                log.error("执行更新操作失败", e);
                result.addFailed(toUpdate.size());
            }
        }
    }

    /**
     * 执行插入操作
     *
     * @param tableConfig 表配置
     * @param data        数据
     */
    private void executeInserts(SyncProperties.TableConfig tableConfig, List<Map<String, Object>> data) {
        if (data.isEmpty()) return;

        String targetTable = tableConfig.getTargetTable();
        Map<String, String> mapping = tableConfig.getMapping();

        // 构建插入SQL
        List<String> targetFields = new ArrayList<>();
        for (String sourceField : mapping.keySet()) {
            targetFields.add(mapping.get(sourceField));
        }

        String fields = String.join(", ", targetFields);
        String placeholders = String.join(", ", Collections.nCopies(targetFields.size(), "?"));

        String sql = "INSERT INTO " + targetTable + " (" + fields + ") VALUES (" + placeholders + ")";
        log.debug("插入SQL: {}", sql);

        // 批量插入
        List<Object[]> batchArgs = new ArrayList<>();
        for (Map<String, Object> row : data) {
            Object[] args = new Object[mapping.size()];
            int i = 0;
            for (String sourceField : mapping.keySet()) {
                args[i++] = row.get(sourceField);
            }
            batchArgs.add(args);
        }

        targetJdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * 执行更新操作
     *
     * @param tableConfig 表配置
     * @param data        数据
     */
    private void executeUpdates(SyncProperties.TableConfig tableConfig, List<Map<String, Object>> data) {
        if (data.isEmpty()) return;

        String targetTable = tableConfig.getTargetTable();
        Map<String, String> mapping = tableConfig.getMapping();
        String idField = tableConfig.getIdField();
        String targetIdField = mapping.get(idField);

        // 构建更新SQL
        List<String> setClause = new ArrayList<>();
        for (String sourceField : mapping.keySet()) {
            if (!sourceField.equals(idField)) {
                String targetField = mapping.get(sourceField);
                setClause.add(targetField + " = ?");
            }
        }

        if (setClause.isEmpty()) {
            log.warn("没有需要更新的字段，跳过更新操作");
            return;
        }

        String sql = "UPDATE " + targetTable + " SET " + String.join(", ", setClause) + " WHERE " + targetIdField + " = ?";
        log.debug("更新SQL: {}", sql);

        // 批量更新
        List<Object[]> batchArgs = new ArrayList<>();
        for (Map<String, Object> row : data) {
            Object[] args = new Object[mapping.size()];
            int i = 0;
            for (String sourceField : mapping.keySet()) {
                if (!sourceField.equals(idField)) {
                    args[i++] = row.get(sourceField);
                }
            }
            // ID放在最后作为WHERE条件
            args[i] = row.get(idField);
            batchArgs.add(args);
        }

        targetJdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * 同步所有配置的表
     *
     * @return 同步结果
     */
    public Map<String, SyncResult> syncAllTables() {
        Map<String, SyncResult> results = new HashMap<>();
        
        for (String tableName : syncProperties.getTables().keySet()) {
            SyncResult result = syncTable(tableName);
            results.put(tableName, result);
        }
        
        return results;
    }

    /**
     * 同步结果类
     */
    @Data
    public static class SyncResult {
        private String tableName;
        private int inserted = 0;
        private int updated = 0;
        private int failed = 0;
        private Exception exception;

        public SyncResult(String tableName) {
            this.tableName = tableName;
        }

        public void addInserted(int count) {
            this.inserted += count;
        }

        public void addUpdated(int count) {
            this.updated += count;
        }

        public void addFailed(int count) {
            this.failed += count;
        }
    }
} 