package com.xuan.tft.tft_backend.repository;

import com.xuan.tft.tft_backend.entity.Comp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompRepository extends JpaRepository<Comp, Long> {

    /**
     * 按羁绊名称搜索阵容：
     * 找出所有包含指定 Trait 的阵容（任意一个棋子具有该羁绊即可）
     */
    @Query("select distinct c from Comp c " +
            "join c.champions ch " +
            "join ch.traits t " +
            "where t.name = :traitName")
    List<Comp> findByTraitName(String traitName);

    /**
     * 按 score 和 likes 排序，返回前 N 个热门阵容
     */
    List<Comp> findTop10ByOrderByScoreDescLikesDesc(); // 先给一个固定Top10方法，后面我们在Service里做limit参数
}
