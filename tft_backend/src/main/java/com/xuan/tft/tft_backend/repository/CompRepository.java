package com.xuan.tft.tft_backend.repository;

import com.xuan.tft.tft_backend.entity.Comp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompRepository extends JpaRepository<Comp, Long> {
    // TODO：按名称查、按分数排序等
}
