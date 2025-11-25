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
import com.xuan.tft.tft_backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new IllegalStateException("当前请求未登录，无法创建阵容");
        }
        User creator = (User) auth.getPrincipal();

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

    /**
     * 计算阵容评分
     * 1. 计算单位得分（基础分 + ability加成）
     * 2. 计算羁绊得分
     * 3. 应用阵容类型加成、前后排平衡、全局ability加成
     */
    private int calculateScore(List<Champion> champions, Map<String, Integer> traitCounts) {
        if (champions.isEmpty()) {
            return 0;
        }

        // 第一步：计算每个单位的基础得分和ability加成
        double totalChampionScore = 0.0;
        Set<String> excludedTraits = new HashSet<>(); // 记录有level=1羁绊的单位，这些羁绊不计入总分
        boolean hasArmorPen = false; // 是否有护甲削减/无视目标部分护甲
        boolean hasMagicPen = false; // 是否有魔抗削减
        boolean hasGrievousWound = false; // 是否有重伤

        for (Champion champion : champions) {
            int cost = champion.getCost() != null ? champion.getCost() : 1;
            double championScore = cost * 10.0; // 基础分：cost * 10

            // 检查ability中的特殊效果
            if (champion.getAbility() != null && champion.getAbility().getName() != null) {
                String abilityText = champion.getAbility().getName();

                // 护甲削减/无视目标部分护甲（全局加成，不叠加）
                // 支持"护甲削减"、"削减护甲"、"无视目标部分护甲"等表述
                if (!hasArmorPen && (abilityText.contains("护甲削减") 
                        || abilityText.contains("削减护甲")
                        || abilityText.contains("无视目标部分护甲"))) {
                    hasArmorPen = true;
                }

                // 魔抗削减（全局加成，不叠加）
                // 支持"魔抗削减"、"削减魔抗"等表述
                if (!hasMagicPen && (abilityText.contains("魔抗削减") 
                        || abilityText.contains("削减魔抗"))) {
                    hasMagicPen = true;
                }

                // 重伤（全局加成，不多次生效）
                if (!hasGrievousWound && abilityText.contains("重伤")) {
                    hasGrievousWound = true;
                }

                // 眩晕（单位自身加成）
                if (abilityText.contains("眩晕")) {
                    championScore *= 1.5;
                }

                // 友军（单位自身加成）
                if (abilityText.contains("友军")) {
                    championScore *= 1.5;
                }
            }

            // 检查是否有level=1的羁绊（单位自身加成，但该羁绊不计入总分）
            // 注意：大宗师是特殊情况，即使有level=1，在1单位或4单位时仍应计入总分
            boolean hasLevel1Trait = false;
            boolean hasGrandmasterLevel1 = false; // 标记是否有大宗师的level=1
            if (champion.getTraits() != null) {
                for (Trait trait : champion.getTraits()) {
                    if (trait.getLevels() != null && trait.getLevels().contains(1)) {
                        hasLevel1Trait = true;
                        // 大宗师是特殊情况，即使有level=1，也不排除（因为它会在1或4单位时生效）
                        if ("大宗师".equals(trait.getName())) {
                            hasGrandmasterLevel1 = true;
                        } else {
                            excludedTraits.add(trait.getName());
                        }
                        break; // 只需要找到一个level=1的羁绊即可
                    }
                }
            }
            // 对于大宗师，即使有level=1，也不对单位分进行1.5倍加成（因为羁绊会正常计入总分）
            if (hasLevel1Trait && !hasGrandmasterLevel1) {
                championScore *= 1.5;
            }

            totalChampionScore += championScore;
        }

        // 计算羁绊得分
        double totalTraitScore = 0.0;
        for (Map.Entry<String, Integer> entry : traitCounts.entrySet()) {
            String traitName = entry.getKey();
            int unitCount = entry.getValue();

            // 如果该羁绊被排除（因为某个单位有level=1的该羁绊），跳过
            if (excludedTraits.contains(traitName)) {
                continue;
            }

            // 找到该羁绊的Trait对象以获取levels
            Trait trait = null;
            for (Champion champion : champions) {
                if (champion.getTraits() != null) {
                    for (Trait t : champion.getTraits()) {
                        if (t.getName().equals(traitName)) {
                            trait = t;
                            break;
                        }
                    }
                }
                if (trait != null) break;
            }

            if (trait == null || trait.getLevels() == null || trait.getLevels().isEmpty()) {
                continue;
            }

            // 检查羁绊是否激活（unitCount >= 最低level）
            int minLevel = trait.getLevels().stream().mapToInt(Integer::intValue).min().orElse(Integer.MAX_VALUE);
            int maxLevel = trait.getLevels().stream().mapToInt(Integer::intValue).max().orElse(1);
            
            if (unitCount < minLevel) {
                continue; // 羁绊未激活，不计分
            }

            // 确定当前激活的level（找到 <= unitCount 的最大level）
            int activatedLevel = minLevel;
            for (int level : trait.getLevels()) {
                if (level <= unitCount && level > activatedLevel) {
                    activatedLevel = level;
                }
            }
            double baseScore;
            if (activatedLevel == maxLevel) {
                baseScore = maxLevel * 5.0; // 最高level：maxLevel * 5
            } else if (activatedLevel == minLevel) {
                baseScore = 10.0; // 最低level：10
            } else {
                // 中间level：在最低和最高之间线性插值
                double minScore = 10.0;
                double maxScore = maxLevel * 5.0;
                if (maxLevel > minLevel) {
                    double ratio = (double)(activatedLevel - minLevel) / (maxLevel - minLevel);
                    baseScore = minScore + (maxScore - minScore) * ratio;
                } else {
                    baseScore = 10.0;
                }
            }

            // 计算加权：阵容中每个拥有该羁绊的单位贡献 0.x（x为单位cost）
            double weight = 0.0;
            for (Champion champion : champions) {
                if (champion.getTraits() != null) {
                    for (Trait t : champion.getTraits()) {
                        if (t.getName().equals(traitName)) {
                            int cost = champion.getCost() != null ? champion.getCost() : 1;
                            weight += cost * 0.1; // 0.x，x为cost
                            break;
                        }
                    }
                }
            }

            // 特殊羁绊处理：兵王/决斗大师在最高层级时额外获得10分基础分（在计算加权前）
            if ("兵王".equals(traitName) || "决斗大师".equals(traitName)) {
                if (activatedLevel == maxLevel) {
                    baseScore += 10.0; // 加在基础分上
                }
            }

            // 特殊羁绊处理：大宗师在1单位或4单位时额外获得基础分（在计算加权前）
            if ("大宗师".equals(traitName)) {
                // 【大宗师】仅在1单位或4单位时生效
                if (unitCount == 1) {
                    // 1单位时：基础分 + 当前阵容中单位数量
                    baseScore += champions.size();
                } else if (unitCount == 4) {
                    // 4单位时：基础分 + 10
                    baseScore += 10.0;
                }
            }

            double traitScore = baseScore * (1.0 + weight);

            // 特殊羁绊处理：大宗师仅在1单位或4单位时生效（如果不在1或4单位，得分为0）
            if ("大宗师".equals(traitName)) {
                if (unitCount != 1 && unitCount != 4) {
                    traitScore = 0;
                }
            }

            totalTraitScore += traitScore;
        }

        // 第三步：计算阵容类型（物理/魔法/混伤）
        double adWeight = 0.0;
        double apWeight = 0.0;
        double totalNonTankWeight = 0.0;

        for (Champion champion : champions) {
            int cost = champion.getCost() != null ? champion.getCost() : 1;
            String roleName = champion.getRole() != null ? champion.getRole().getName() : "";

            // 忽略Tank单位
            if (roleName.contains("Tank")) {
                continue;
            }

            totalNonTankWeight += cost;

            if (roleName.startsWith("AD")) {
                adWeight += cost;
            } else if (roleName.startsWith("AP")) {
                apWeight += cost;
            }
            // Mix单位不单独计算，会自然形成混伤
        }

        // 确定阵容类型
        String compType = "MIXED"; // 默认混伤
        if (totalNonTankWeight > 0) {
            double adRatio = adWeight / totalNonTankWeight;
            double apRatio = apWeight / totalNonTankWeight;

            if (adRatio >= 0.7) {
                compType = "AD"; // 物理阵容
            } else if (apRatio >= 0.7) {
                compType = "AP"; // 魔法阵容
            } else {
                compType = "MIXED"; // 混伤阵容
            }
        }

        double compTypeMultiplier = 1.0; // 可以调整加成

        // 第四步：检查前后排分布
        int frontLineCount = 0; // Tank + Fighter
        int backLineCount = 0; // 其他

        for (Champion champion : champions) {
            String roleName = champion.getRole() != null ? champion.getRole().getName() : "";
            if (roleName.contains("Tank") || roleName.contains("Fighter")) {
                frontLineCount++;
            } else {
                backLineCount++;
            }
        }

        double frontBackMultiplier = 1.0;
        if (frontLineCount < backLineCount - 3) {
            frontBackMultiplier = 0.85;
        }

        // 第五步：应用全局ability加成（根据阵容类型）
        double globalMultiplier = 1.0;
        // 护甲削减/无视目标部分护甲：只适用于物理阵容/混合阵容
        if (hasArmorPen && (compType.equals("AD") || compType.equals("MIXED"))) {
            globalMultiplier *= 1.2;
        }
        // 魔抗削减：只适用于魔法阵容/混合阵容
        if (hasMagicPen && (compType.equals("AP") || compType.equals("MIXED"))) {
            globalMultiplier *= 1.2;
        }
        // 重伤：适用于所有阵容
        if (hasGrievousWound) {
            globalMultiplier *= 1.2;
        }

        // 最终得分
        double finalScore = (totalChampionScore + totalTraitScore) 
                * compTypeMultiplier 
                * frontBackMultiplier 
                * globalMultiplier;

        return (int) Math.round(finalScore);
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

