package com.whoiszxl.datasync;

import com.whoiszxl.datasync.config.SyncProperties;
import com.whoiszxl.datasync.service.DataSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数据同步应用程序入口
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.whoiszxl.datasync", "com.whoiszxl.core"})
public class DataSyncApplication implements CommandLineRunner {

    @Autowired
    private DataSyncService dataSyncService;

    @Autowired
    private SyncProperties syncProperties;

    public static void main(String[] args) {
        SpringApplication.run(DataSyncApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (syncProperties.isAutoSync()) {
            log.info("自动同步启动，开始同步数据...");
            dataSyncService.syncAllTables();
            log.info("自动同步完成！");
        } else {
            log.info("自动同步已禁用，可通过API手动触发同步");
        }
    }
} 