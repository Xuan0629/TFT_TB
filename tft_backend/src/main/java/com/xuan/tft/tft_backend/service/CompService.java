package com.xuan.tft.tft_backend.service;

import com.xuan.tft.tft_backend.dto.CompCreateRequest;
import com.xuan.tft.tft_backend.entity.Champion;
import com.xuan.tft.tft_backend.entity.Comp;
import com.xuan.tft.tft_backend.entity.Trait;
import com.xuan.tft.tft_backend.repository.ChampionRepository;
import com.xuan.tft.tft_backend.repository.CompRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xuan.tft.tft_backend.dto.ChampionBasicDto;
import com.xuan.tft.tft_backend.dto.CompSummaryDto;
import com.xuan.tft.tft_backend.dto.TraitCountDto;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompService {

    private final ChampionRepository championRepository;
    private final CompRepository compRepository;
    private final UserService userService;

    public CompService(ChampionRepository championRepository,
                       CompRepository compRepository,
                       UserService userService) {
        this.championRepository = championRepository;
        this.compRepository = compRepository;
        this.userService = userService;
    }

    @Transactional
    public CompSummaryDto createComp(CompCreateRequest request) {
        List<Champion> champions = championRepository.findAllById(request.getChampionIds());
        if (champions.isEmpty()) {
            throw new IllegalArgumentException("阵容中至少需要一个有效的棋子 ID。");
        }

        // 找到创建者用户（JWT相关代码编写后修改）
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("创建阵容需要提供 userId");
        }
        var creator = userService.findById(request.getUserId());


        Map<String, Integer> traitCountMap = countTraits(champions);
        int score = calculateScore(champions, traitCountMap);

        Comp comp = new Comp();
        comp.setName(
                request.getName() != null && !request.getName().isEmpty()
                        ? request.getName()
                        : generateDefaultName(traitCountMap)
        );
        comp.setDescription(request.getDescription());
        comp.setScore(score);
        comp.getChampions().addAll(champions);
        comp.setCreator(creator);

        Comp saved = compRepository.save(comp);

        return buildCompSummaryDto(saved, traitCountMap);
    }

    //测试用
    public CompSummaryDto previewComp(CompCreateRequest request) {
        List<Champion> champions = championRepository.findAllById(request.getChampionIds());
        if (champions.isEmpty()) {
            throw new IllegalArgumentException("阵容中至少需要一个有效的棋子 ID。");
        }

        Map<String, Integer> traitCountMap = countTraits(champions);
        int score = calculateScore(champions, traitCountMap);

        String name = (request.getName() != null && !request.getName().isEmpty())
                ? request.getName()
                : generateDefaultName(traitCountMap);

        return buildCompSummaryDto(
                null,
                name,
                request.getDescription(),
                score,
                champions,
                traitCountMap
        );
    }

    public List<Comp> getAllComps() {
        return compRepository.findAll();
    }

    public Optional<Comp> getCompById(Long id) {
        return compRepository.findById(id);
    }


    private Map<String, Integer> countTraits(List<Champion> champions) {
        Map<String, Integer> traitCountMap = new HashMap<>();
        for (Champion champion : champions) {
            for (Trait trait : champion.getTraits()) {
                traitCountMap.merge(trait.getName(), 1, Integer::sum);
            }
        }
        return traitCountMap;
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

        // 羁绊加分：数量越多加分越多
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

    // 如果用户没填 ，用羁绊组合默认生成
    private String generateDefaultName(Map<String, Integer> traitCounts) {
        if (traitCounts.isEmpty()) {
            return "Custom Comp";
        }
        // 取前几个羁绊名
        return String.join(" ",
                traitCounts.keySet().stream().limit(3).toList()
        ) + " Comp";
    }

    private CompSummaryDto buildCompSummaryDto(Comp comp, Map<String, Integer> traitCountMap) {
        return buildCompSummaryDto(
                comp.getId(),
                comp.getName(),
                comp.getDescription(),
                comp.getScore(),
                new ArrayList<>(comp.getChampions()),
                traitCountMap
        );
    }

    private CompSummaryDto buildCompSummaryDto(Long id,
                                               String name,
                                               String description,
                                               Integer score,
                                               List<Champion> champions,
                                               Map<String, Integer> traitCountMap) {

        List<ChampionBasicDto> championDtos = champions.stream()
                .map(champion -> new ChampionBasicDto(
                        champion.getId(),
                        champion.getName(),
                        champion.getCost(),
                        champion.getTraits()
                                .stream()
                                .map(Trait::getName)
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());

        List<TraitCountDto> traitDtos = traitCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> new TraitCountDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new CompSummaryDto(
                id,
                name,
                description,
                score,
                championDtos,
                traitDtos
        );
    }


    //按羁绊名称搜索阵容，并生成总结 DTO 列表
    public List<CompSummaryDto> searchByTrait(String traitName) {
        List<Comp> comps = compRepository.findByTraitName(traitName);
        List<CompSummaryDto> result = new ArrayList<>();

        for (Comp comp : comps) {
            Map<String, Integer> traitCounts = countTraits(new ArrayList<>(comp.getChampions()));
            result.add(buildCompSummaryDto(comp, traitCounts));
        }
        return result;
    }

    //获取热门阵容（测试版：按 score + likes 排序，取前 N 个）
    public List<CompSummaryDto> getTopComps(int limit) {

        List<Comp> comps = compRepository.findTop10ByOrderByScoreDescLikesDesc();
        if (limit > 0 && comps.size() > limit) {
            comps = comps.subList(0, limit);
        }

        List<CompSummaryDto> result = new ArrayList<>();
        for (Comp comp : comps) {
            Map<String, Integer> traitCounts = countTraits(new ArrayList<>(comp.getChampions()));
            result.add(buildCompSummaryDto(comp, traitCounts));
        }
        return result;
    }

    @Transactional
    public CompSummaryDto likeComp(Long id) {
        Comp comp = compRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到指定 ID 的阵容：" + id));
        comp.setLikes(comp.getLikes() + 1);
        Comp saved = compRepository.save(comp);

        Map<String, Integer> traitCounts = countTraits(new ArrayList<>(saved.getChampions()));
        return buildCompSummaryDto(saved, traitCounts);
    }

    @Transactional
    public void increaseUsage(Long id) {
        compRepository.findById(id).ifPresent(comp -> {
            comp.setUsageCount(comp.getUsageCount() + 1);
            compRepository.save(comp);
        });
    }
}
