package com.xuan.tft.tft_backend.repository;

import com.xuan.tft.tft_backend.entity.Champion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChampionRepository extends JpaRepository<Champion, Long> {

    // TODO：自定义查询，e.g.按 cost 查、按名字查
    Champion findByName(String name);
}
