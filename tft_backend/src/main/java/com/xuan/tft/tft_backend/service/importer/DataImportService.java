package com.xuan.tft.tft_backend.service.importer;

import com.xuan.tft.tft_backend.entity.*;
import com.xuan.tft.tft_backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class DataImportService {

    private final TraitRepository traitRepository;
    private final ChampionRepository championRepository;
    private final RoleRepository roleRepository;
    private final AbilityRepository abilityRepository;
    private final ResourceLoader resourceLoader;

    public DataImportService(TraitRepository traitRepository,
                             ChampionRepository championRepository,
                             RoleRepository roleRepository,
                             AbilityRepository abilityRepository,
                             ResourceLoader resourceLoader) {
        this.traitRepository = traitRepository;
        this.championRepository = championRepository;
        this.roleRepository = roleRepository;
        this.abilityRepository = abilityRepository;
        this.resourceLoader = resourceLoader;
    }

    @Transactional
    public void importCdragonFromClasspath(String classpath, String setName) throws Exception {
        Resource res = resourceLoader.getResource("classpath:" + classpath);
        if (!res.exists()) {
            throw new IllegalArgumentException("数据文件不存在: classpath:" + classpath);
        }

        // 为避免重复数据，先清空该赛季已存在的 champions 与 traits（级联删除关联表/trait_levels）
        championRepository.deleteBySetName(setName);
        traitRepository.deleteBySetName(setName);

        try (InputStream is = res.getInputStream()) {
            CdragonParser parser = new CdragonParser(setName);
            CdragonParser.RawSet raw = parser.parseCdragon(is);

            // 1) upsert traits（含 levels）
            // 先收集所有在 champions 中出现的 trait 名称，确保它们都被导入
            Set<String> allTraitNames = new HashSet<>();
            for (CdragonParser.RawChampion c : raw.champions()) {
                if (c.traitNames() != null) {
                    for (String tn : c.traitNames()) {
                        if (tn != null && !tn.isBlank()) {
                            allTraitNames.add(tn);
                        }
                    }
                }
            }
            
            Map<String, Trait> traitMap = new HashMap<>();
            // 先处理从 traits 数组中解析的 traits
            for (CdragonParser.RawTrait t : raw.traits()) {
                if (t.name() == null || t.name().isBlank()) continue;
                
                Trait db = traitRepository.findBySetNameAndName(t.setName(), t.name())
                        .orElseGet(() -> {
                            Trait nt = new Trait();
                            nt.setSetName(t.setName());
                            nt.setName(t.name());
                            return nt;
                        });
                db.setDescription(t.description());
                
                // 去重并保持顺序，避免来源数据重复导致 trait_levels 多次插入
                db.getLevels().clear();
                if (t.levels() != null && !t.levels().isEmpty()) {
                    LinkedHashSet<Integer> unique = new LinkedHashSet<>(t.levels());
                    db.getLevels().addAll(unique);
                }
                
                db = traitRepository.save(db);
                traitMap.put(key(t.setName(), t.name()), db);
            }
            
            // 确保所有在 champions 中出现的 traits 都存在于 traitMap 中
            // 如果某个 trait 只在 champions 中出现而没有在 traits 数组中定义，创建它
            for (String traitName : allTraitNames) {
                String mapKey = key(setName, traitName);
                if (!traitMap.containsKey(mapKey)) {
                    Trait db = traitRepository.findBySetNameAndName(setName, traitName)
                            .orElseGet(() -> {
                                Trait nt = new Trait();
                                nt.setSetName(setName);
                                nt.setName(traitName);
                                return nt;
                            });
                    if (db.getLevels() == null) {
                        db.setLevels(new ArrayList<>());
                    }
                    db = traitRepository.save(db);
                    traitMap.put(mapKey, db);
                }
            }

            // 2) 准备 Role / Ability 映射（upsert）
            Map<String, Role> roleMap = new HashMap<>();
            Map<String, Ability> abilityMap = new HashMap<>();

            // 3) upsert champions（含 role/ability + 关系）
            for (CdragonParser.RawChampion c : raw.champions()) {
                Champion db = championRepository.findBySetNameAndName(c.setName(), c.name())
                        .orElseGet(() -> {
                            Champion nc = new Champion();
                            nc.setSetName(c.setName());
                            nc.setName(c.name());
                            return nc;
                        });

                db.setCost(c.cost() != null ? c.cost() : 1);

                // 设置 role
                if (c.roleName() != null && !c.roleName().isBlank()) {
                    Role r = roleMap.computeIfAbsent(c.roleName(), rn ->
                            roleRepository.findByName(rn).orElseGet(() -> {
                                Role nr = new Role();
                                nr.setName(rn);
                                return roleRepository.save(nr);
                            }));
                    db.setRole(r);
                } else {
                    db.setRole(null); // 未提供则清空
                }

                // 设置 ability
                if (c.abilityName() != null && !c.abilityName().isBlank()) {
                    Ability a = abilityMap.computeIfAbsent(c.abilityName(), an ->
                            abilityRepository.findByName(an).orElseGet(() -> {
                                Ability na = new Ability();
                                na.setName(an);
                                return abilityRepository.save(na);
                            }));
                    db.setAbility(a);
                } else {
                    db.setAbility(null);
                }

                // 处理多对多关系：先清空再重建 traits（幂等）
                // 对于多对多关系，需要替换整个集合以确保正确更新
                Set<Trait> newTraits = new HashSet<>();
                if (c.traitNames() != null && !c.traitNames().isEmpty()) {
                    for (String tn : c.traitNames()) {
                        // 过滤空字符串和 null
                        if (tn == null || tn.isBlank()) continue;
                        
                        Trait t = traitMap.get(key(c.setName(), tn));
                        if (t != null) {
                            newTraits.add(t);
                        } else {
                            // 如果 trait 不在 traitMap 中，尝试从数据库查找或创建
                            Trait foundTrait = traitRepository.findBySetNameAndName(c.setName(), tn)
                                    .orElseGet(() -> {
                                        Trait nt = new Trait();
                                        nt.setSetName(c.setName());
                                        nt.setName(tn);
                                        return traitRepository.save(nt);
                                    });
                            newTraits.add(foundTrait);
                            traitMap.put(key(c.setName(), tn), foundTrait);
                        }
                    }
                }
                // 替换整个集合以确保多对多关系正确更新
                db.setTraits(newTraits);

                championRepository.save(db);
            }
        }
    }

    private static String key(String set, String name) {
        return set + "::" + name;
    }
}
