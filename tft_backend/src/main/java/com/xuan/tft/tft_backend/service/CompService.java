package com.xuan.tft.tft_backend.service;

import com.xuan.tft.tft_backend.dto.CompCreateRequest;
import com.xuan.tft.tft_backend.entity.Champion;
import com.xuan.tft.tft_backend.entity.Comp;
import com.xuan.tft.tft_backend.entity.Trait;
import com.xuan.tft.tft_backend.repository.ChampionRepository;
import com.xuan.tft.tft_backend.repository.CompRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CompService {

    private final ChampionRepository championRepository;
    private final CompRepository compRepository;

    public CompService(ChampionRepository championRepository,
                       CompRepository compRepository) {
        this.championRepository = championRepository;
        this.compRepository = compRepository;
    }

    @Transactional
    public Comp createComp(CompCreateRequest request) {
        // 1. 根据传入的 championIds 查询棋子
        List<Champion> champions = championRepository.findAllById(request.getChampionIds());

        if (champions.isEmpty()) {
            throw new IllegalArgumentException("阵容中至少需要一个有效的棋子 ID。");
        }

        // 2. 统计羁绊出现次数
        Map<String, Integer> traitCountMap = new HashMap<>();
        for (Champion champion : champions) {
            for (Trait trait : champion.getTraits()) {
                traitCountMap.merge(trait.getName(), 1, Integer::sum);
            }
        }

        // 3. 计算一个简单的评分
        int score = calculateScore(champions, traitCountMap);

        // 4. 构建 Comp 实体并保存
        Comp comp = new Comp();
        comp.setName(
                request.getName() != null && !request.getName().isEmpty()
                        ? request.getName()
                        : generateDefaultName(traitCountMap)
        );
        comp.setDescription(request.getDescription());
        comp.setScore(score);
        comp.getChampions().addAll(champions);

        return compRepository.save(comp);
    }

    //简单算法，后续更新
    private int calculateScore(List<Champion> champions, Map<String, Integer> traitCounts) {
        int score = 0;

        // 基础分：每个棋子的 cost * 10
        for (Champion champion : champions) {
            if (champion.getCost() != null) {
                score += champion.getCost() * 10;
            } else {
                score += 5; // 没 cost 就给个默认分
            }
        }

        // 羁绊加分：数量越多加分越多（只是一个示例逻辑，之后可以按你喜好调整）
        for (Integer count : traitCounts.values()) {
            if (count >= 2 && count < 4) {
                score += 10;
            } else if (count >= 4 && count < 6) {
                score += 20;
            } else if (count >= 6) {
                score += 30;
            }
        }

        return score;
    }

    // 如果用户没填 name，用羁绊组合生成一个默认名字
    private String generateDefaultName(Map<String, Integer> traitCounts) {
        if (traitCounts.isEmpty()) {
            return "Custom Comp";
        }
        // 取前几个羁绊名拼一下
        return String.join(" ",
                traitCounts.keySet().stream().limit(3).toList()
        ) + " Comp";
    }

    public List<Comp> getAllComps() {
        return compRepository.findAll();
    }

    public Optional<Comp> getCompById(Long id) {
        return compRepository.findById(id);
    }
}
