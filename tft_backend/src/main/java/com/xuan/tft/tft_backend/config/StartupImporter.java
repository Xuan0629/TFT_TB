package com.xuan.tft.tft_backend.config;

import com.xuan.tft.tft_backend.repository.ChampionRepository;
import com.xuan.tft.tft_backend.repository.TraitRepository;
import com.xuan.tft.tft_backend.service.importer.DataImportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用启动后，按配置选择性导入 S15 数据（幂等）
 */
@Configuration
public class StartupImporter {

    @Value("${tft.import.enabled:true}")
    private boolean enabled;

    @Value("${tft.import.classpath:data/tft_s15_zh_cn.json}")
    private String importClasspath;

    @Value("${tft.import.set-name:S15}")
    private String setName;

    @Bean
    CommandLineRunner tftSeedRunner(DataImportService importer,
                                    TraitRepository traitRepo,
                                    ChampionRepository champRepo) {
        return args -> {
            if (!enabled) return;

            long traitCount = traitRepo.countBySetName(setName);
            long champCount = champRepo.countBySetName(setName);
            if (traitCount == 0 && champCount == 0) {
                try {
                    importer.importCdragonFromClasspath(importClasspath, setName);
                    System.out.printf("[TFT Import] Imported set=%s from classpath=%s%n", setName, importClasspath);
                } catch (IllegalArgumentException ex) {
                    System.err.printf("[TFT Import][WARN] %s%n", ex.getMessage());
                    System.err.printf("[TFT Import][WARN] 已跳过导入，应用继续启动。请更换为包含 traits[]/units[]/champions[] 的 S15 静态 JSON。当前配置: set=%s, classpath=%s%n",
                            setName, importClasspath);
                }
            } else {
                System.out.printf("[TFT Import] Skip. Existing set=%s data: traits=%d, champions=%d%n",
                        setName, traitCount, champCount);
            }
        };
    }
}
