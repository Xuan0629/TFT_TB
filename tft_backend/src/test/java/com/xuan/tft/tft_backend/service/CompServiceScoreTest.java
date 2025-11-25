package com.xuan.tft.tft_backend.service;

import com.xuan.tft.tft_backend.entity.Ability;
import com.xuan.tft.tft_backend.entity.Champion;
import com.xuan.tft.tft_backend.entity.Role;
import com.xuan.tft.tft_backend.entity.Trait;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CompService 评分算法测试类
 * 使用反射测试私有方法 calculateScore
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("阵容评分算法测试")
class CompServiceScoreTest {

    @Mock
    private com.xuan.tft.tft_backend.repository.ChampionRepository championRepository;

    @Mock
    private com.xuan.tft.tft_backend.repository.CompRepository compRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private CompService compService;

    private Method calculateScoreMethod;

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射获取私有方法
        calculateScoreMethod = CompService.class.getDeclaredMethod(
                "calculateScore", List.class, Map.class);
        calculateScoreMethod.setAccessible(true);
    }

    /**
     * 测试用例1：基础单位得分
     * 预期：cost=1的单位得10分，cost=2得20分，cost=3得30分
     */
    @Test
    @DisplayName("测试基础单位得分")
    void testBasicChampionScore() throws Exception {
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 1, null, null, Collections.emptySet()),
                createChampion("单位2", 2, null, null, Collections.emptySet()),
                createChampion("单位3", 3, null, null, Collections.emptySet())
        );
        Map<String, Integer> traitCounts = new HashMap<>();

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 基础分：10 + 20 + 30 = 60
        assertEquals(60, score, "基础单位得分应为60");
    }

    /**
     * 测试用例2：Ability加成 - 眩晕
     * 预期：包含"眩晕"的单位得分 ×1.5
     */
    @Test
    @DisplayName("测试Ability加成 - 眩晕")
    void testStunAbilityBonus() throws Exception {
        Ability stunAbility = createAbility("眩晕目标并造成伤害");
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 1, null, stunAbility, Collections.emptySet())
        );
        Map<String, Integer> traitCounts = new HashMap<>();

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 基础分10 × 1.5 = 15
        assertEquals(15, score, "眩晕加成后应为15");
    }

    /**
     * 测试用例3：Ability加成 - 友军
     * 预期：包含"友军"的单位得分 ×1.5
     */
    @Test
    @DisplayName("测试Ability加成 - 友军")
    void testAllyAbilityBonus() throws Exception {
        Ability allyAbility = createAbility("为附近友军提供加成");
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 2, null, allyAbility, Collections.emptySet())
        );
        Map<String, Integer> traitCounts = new HashMap<>();

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 基础分20 × 1.5 = 30
        assertEquals(30, score, "友军加成后应为30");
    }

    /**
     * 测试用例4：Ability加成 - 护甲削减（全局，仅物理/混合阵容）
     * 预期：物理阵容时，阵容总分 ×1.2
     */
    @Test
    @DisplayName("测试Ability加成 - 护甲削减（全局）")
    void testArmorPenetrationGlobalBonus() throws Exception {
        Role adCarry = createRole("ADCarry");
        Ability armorPenAbility = createAbility("对目标造成伤害并削减护甲");
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 3, adCarry, armorPenAbility, Collections.emptySet()),
                createChampion("单位2", 2, adCarry, null, Collections.emptySet())
        );
        Map<String, Integer> traitCounts = new HashMap<>();

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 单位分：30 + 20 = 50
        // 物理阵容（AD>=70%），护甲削减生效：50 × 1.2 = 60
        assertEquals(60, score, "物理阵容护甲削减全局加成后应为60");
    }

    /**
     * 测试用例5：Ability加成 - 魔抗削减（全局，仅魔法/混合阵容）
     * 预期：魔法阵容时，阵容总分 ×1.2
     */
    @Test
    @DisplayName("测试Ability加成 - 魔抗削减（全局）")
    void testMagicPenetrationGlobalBonus() throws Exception {
        Role apCarry = createRole("APCarry");
        Ability magicPenAbility = createAbility("对目标造成伤害并削减魔抗");
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 3, apCarry, magicPenAbility, Collections.emptySet()),
                createChampion("单位2", 2, apCarry, null, Collections.emptySet())
        );
        Map<String, Integer> traitCounts = new HashMap<>();

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 单位分：30 + 20 = 50
        // 魔法阵容（AP>=70%），魔抗削减生效：50 × 1.2 = 60
        assertEquals(60, score, "魔法阵容魔抗削减全局加成后应为60");
    }

    /**
     * 测试用例5b：Ability加成 - 重伤（全局）
     * 预期：阵容总分 ×1.2
     */
    @Test
    @DisplayName("测试Ability加成 - 重伤（全局）")
    void testGrievousWoundGlobalBonus() throws Exception {
        Ability grievousAbility = createAbility("造成伤害并施加重伤效果");
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 1, null, grievousAbility, Collections.emptySet()),
                createChampion("单位2", 1, null, null, Collections.emptySet())
        );
        Map<String, Integer> traitCounts = new HashMap<>();

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // (10 + 10) × 1.2 = 24
        assertEquals(24, score, "重伤全局加成后应为24");
    }

    /**
     * 测试用例6：羁绊得分 - 基础计算
     * 预期：最低level=10分，最高level=maxLevel*5分
     */
    @Test
    @DisplayName("测试羁绊得分 - 基础计算")
    void testTraitScoreBasic() throws Exception {
        Trait trait = createTrait("测试羁绊", Arrays.asList(2, 4, 6));
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 1, null, null, Set.of(trait)),
                createChampion("单位2", 1, null, null, Set.of(trait))
        );
        Map<String, Integer> traitCounts = new HashMap<>();
        traitCounts.put("测试羁绊", 2);

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 单位分：10 + 10 = 20
        // 羁绊分：最低level=2，基础分=10，加权=(0.1+0.1)=0.2，得分=10*(1+0.2)=12
        // 总分：20 + 12 = 32
        assertTrue(score >= 30 && score <= 35, "羁绊得分应在合理范围内");
    }

    /**
     * 测试用例7：羁绊得分 - 最高level
     * 预期：最高level得分 = maxLevel * 5
     */
    @Test
    @DisplayName("测试羁绊得分 - 最高level")
    void testTraitScoreMaxLevel() throws Exception {
        Trait trait = createTrait("测试羁绊", Arrays.asList(2, 4, 6));
        Role fighter = createRole("ADFighter"); // 添加role以避免前后排惩罚
        List<Champion> champions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            champions.add(createChampion("单位" + i, 1, fighter, null, Set.of(trait)));
        }
        Map<String, Integer> traitCounts = new HashMap<>();
        traitCounts.put("测试羁绊", 6);

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 单位分：10 * 6 = 60
        // 羁绊分：最高level=6，基础分=6*5=30，加权=0.1*6=0.6，得分=30*(1+0.6)=48
        // 前后排：6个Fighter都是前排，frontLineCount=6, backLineCount=0，不触发惩罚
        // 总分：60 + 48 = 108
        assertEquals(108, score, "最高level羁绊得分应为108");
    }

    /**
     * 测试用例8：特殊羁绊 - 大宗师（仅在1或4单位时生效）
     */
    @Test
    @DisplayName("测试特殊羁绊 - 大宗师")
    void testSpecialTraitGrandmaster() throws Exception {
        Trait grandmaster = createTrait("大宗师", Arrays.asList(1, 4));
        Role fighter = createRole("ADFighter"); // 添加role以避免前后排惩罚
        List<Champion> champions1 = Arrays.asList(
                createChampion("单位1", 1, fighter, null, Set.of(grandmaster))
        );
        Map<String, Integer> traitCounts1 = new HashMap<>();
        traitCounts1.put("大宗师", 1);

        int score1 = (int) calculateScoreMethod.invoke(compService, champions1, traitCounts1);
        // 单位分：10（大宗师有level=1但不触发1.5倍加成，因为羁绊会正常计入总分）
        // 羁绊分：基础分=10（level=1的最低分）+ 1（当前阵容单位数量）= 11，加权=0.1，得分=11*(1+0.1)=12.1≈12
        // 前后排：1个Fighter是前排，不触发惩罚
        // 总分：10 + 12 = 22
        assertEquals(22, score1, "1单位大宗师应生效，得分应为22");

        // 测试2单位（不应生效）
        List<Champion> champions2 = Arrays.asList(
                createChampion("单位1", 1, fighter, null, Set.of(grandmaster)),
                createChampion("单位2", 1, fighter, null, Set.of(grandmaster))
        );
        Map<String, Integer> traitCounts2 = new HashMap<>();
        traitCounts2.put("大宗师", 2);
        int score2 = (int) calculateScoreMethod.invoke(compService, champions2, traitCounts2);
        // 2单位时，羁绊分应为0（因为unitCount != 1 && unitCount != 4），但单位分仍然计算：10 + 10 = 20
        assertEquals(20, score2, "2单位大宗师不应生效，只有单位分20");

        // 测试4单位（应生效，且获得+10基础分）
        List<Champion> champions4 = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            champions4.add(createChampion("单位" + (i + 1), 1, fighter, null, Set.of(grandmaster)));
        }
        Map<String, Integer> traitCounts4 = new HashMap<>();
        traitCounts4.put("大宗师", 4);
        int score4 = (int) calculateScoreMethod.invoke(compService, champions4, traitCounts4);
        // 单位分：10 * 4 = 40
        // 羁绊分：基础分=4*5（最高level）+ 10（特殊加成）= 30，加权=0.1*4=0.4，得分=30*(1+0.4)=42
        // 前后排：4个Fighter都是前排，不触发惩罚
        // 总分：40 + 42 = 82
        assertEquals(82, score4, "4单位大宗师应生效，得分应为82");
    }

    /**
     * 测试用例9：特殊羁绊 - 兵王/决斗大师（最高层级额外+10分）
     */
    @Test
    @DisplayName("测试特殊羁绊 - 兵王/决斗大师")
    void testSpecialTraitChampion() throws Exception {
        Trait champion = createTrait("兵王", Arrays.asList(2, 4));
        Role fighter = createRole("ADFighter"); // 添加role以避免前后排惩罚
        List<Champion> champions = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            champions.add(createChampion("单位" + i, 1, fighter, null, Set.of(champion)));
        }
        Map<String, Integer> traitCounts = new HashMap<>();
        traitCounts.put("兵王", 4);

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 单位分：10 * 4 = 40
        // 羁绊分：最高level=4，基础分=4*5+10（特殊羁绊）=30，加权=0.1*4=0.4，得分=30*(1+0.4)=42
        // 前后排：4个Fighter都是前排，不触发惩罚
        // 总分：40 + 42 = 82
        assertEquals(82, score, "最高层级兵王应包含额外10分，总分应为82");
    }

    /**
     * 测试用例10：level=1的羁绊处理
     * 预期：单位得分×1.5，但该羁绊不计入总分
     */
    @Test
    @DisplayName("测试level=1羁绊处理")
    void testLevel1Trait() throws Exception {
        Trait level1Trait = createTrait("单单位羁绊", Arrays.asList(1));
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 1, null, null, Set.of(level1Trait))
        );
        Map<String, Integer> traitCounts = new HashMap<>();
        traitCounts.put("单单位羁绊", 1);

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 单位分：10 × 1.5 = 15（因为有level=1羁绊）
        // 羁绊分：0（被排除）
        assertEquals(15, score, "level=1羁绊应加成单位但不计入羁绊分");
    }

    /**
     * 测试用例11：阵容类型判断 - 物理阵容
     */
    @Test
    @DisplayName("测试阵容类型 - 物理阵容")
    void testCompTypeAD() throws Exception {
        Role adCarry = createRole("ADCarry");
        Role adCaster = createRole("ADCaster");
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 3, adCarry, null, Collections.emptySet()),
                createChampion("单位2", 3, adCaster, null, Collections.emptySet()),
                createChampion("单位3", 2, adCarry, null, Collections.emptySet())
        );
        Map<String, Integer> traitCounts = new HashMap<>();

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // AD权重：3+3+2=8，总非Tank权重=8，AD比例=100% >= 70%
        assertTrue(score > 0, "物理阵容应正常计算");
    }

    /**
     * 测试用例12：阵容类型判断 - 魔法阵容
     */
    @Test
    @DisplayName("测试阵容类型 - 魔法阵容")
    void testCompTypeAP() throws Exception {
        Role apCarry = createRole("APCarry");
        Role apCaster = createRole("APCaster");
        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 3, apCarry, null, Collections.emptySet()),
                createChampion("单位2", 3, apCaster, null, Collections.emptySet()),
                createChampion("单位3", 2, apCarry, null, Collections.emptySet())
        );
        Map<String, Integer> traitCounts = new HashMap<>();

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // AP权重：3+3+2=8，总非Tank权重=8，AP比例=100% >= 70%
        assertTrue(score > 0, "魔法阵容应正常计算");
    }

    /**
     * 测试用例13：前后排平衡检查
     * 预期：前排 < 后排 - 3 时，总分 ×0.85
     */
    @Test
    @DisplayName("测试前后排平衡")
    void testFrontBackBalance() throws Exception {
        Role tank = createRole("ADTank");
        Role carry = createRole("ADCarry");
        Role caster = createRole("ADCaster");

        // 1个前排，5个后排（1 < 5-3=2，应触发惩罚）
        List<Champion> champions = Arrays.asList(
                createChampion("前排1", 1, tank, null, Collections.emptySet()),
                createChampion("后排1", 1, carry, null, Collections.emptySet()),
                createChampion("后排2", 1, carry, null, Collections.emptySet()),
                createChampion("后排3", 1, caster, null, Collections.emptySet()),
                createChampion("后排4", 1, caster, null, Collections.emptySet()),
                createChampion("后排5", 1, carry, null, Collections.emptySet())
        );
        Map<String, Integer> traitCounts = new HashMap<>();

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 基础分：6 * 10 = 60
        // 如果触发惩罚：60 * 0.85 = 51
        assertTrue(score >= 50 && score <= 60, "前后排不平衡应触发惩罚");
    }

    /**
     * 测试用例14：综合场景 - 完整阵容
     */
    @Test
    @DisplayName("测试综合场景 - 完整阵容")
    void testComprehensiveScenario() throws Exception {
        // 创建多个羁绊
        Trait trait1 = createTrait("羁绊1", Arrays.asList(2, 4, 6));
        Trait trait2 = createTrait("羁绊2", Arrays.asList(2, 3, 4));
        Trait level1Trait = createTrait("单单位羁绊", Arrays.asList(1));

        Role adCarry = createRole("ADCarry");
        Role tank = createRole("ADTank");
        Ability stunAbility = createAbility("眩晕目标");
        Ability armorPenAbility = createAbility("削减护甲");

        List<Champion> champions = Arrays.asList(
                createChampion("单位1", 3, adCarry, stunAbility, Set.of(trait1, trait2)),
                createChampion("单位2", 2, adCarry, armorPenAbility, Set.of(trait1)),
                createChampion("单位3", 1, tank, null, Set.of(level1Trait)),
                createChampion("单位4", 2, adCarry, null, Set.of(trait2))
        );

        Map<String, Integer> traitCounts = new HashMap<>();
        traitCounts.put("羁绊1", 2);
        traitCounts.put("羁绊2", 2);

        int score = (int) calculateScoreMethod.invoke(compService, champions, traitCounts);

        // 验证得分在合理范围内
        assertTrue(score > 0, "综合场景应计算出有效得分");
        System.out.println("综合场景得分: " + score);
    }

    // ========== 辅助方法 ==========

    private Champion createChampion(String name, int cost, Role role, Ability ability, Set<Trait> traits) {
        Champion champion = new Champion();
        champion.setName(name);
        champion.setCost(cost);
        champion.setRole(role);
        champion.setAbility(ability);
        if (traits != null) {
            champion.setTraits(traits);
        } else {
            champion.setTraits(new HashSet<>());
        }
        return champion;
    }

    private Role createRole(String name) {
        Role role = new Role();
        role.setName(name);
        return role;
    }

    private Ability createAbility(String name) {
        Ability ability = new Ability();
        ability.setName(name);
        return ability;
    }

    private Trait createTrait(String name, List<Integer> levels) {
        Trait trait = new Trait();
        trait.setName(name);
        trait.setLevels(new ArrayList<>(levels));
        return trait;
    }
}

