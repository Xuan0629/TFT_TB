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

        championRepository.deleteBySetName(setName);
        traitRepository.deleteBySetName(setName);

        try (InputStream is = res.getInputStream()) {
            CdragonParser parser = new CdragonParser(setName);
            CdragonParser.RawSet raw = parser.parseCdragon(is);

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

                db.getLevels().clear();
                if (t.levels() != null && !t.levels().isEmpty()) {
                    LinkedHashSet<Integer> unique = new LinkedHashSet<>(t.levels());
                    db.getLevels().addAll(unique);
                }

                db = traitRepository.save(db);
                traitMap.put(key(t.setName(), t.name()), db);

            }

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

            Map<String, Role> roleMap = new HashMap<>();
            Map<String, Ability> abilityMap = new HashMap<>();

            // upsert champions
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

                db.setTraits(newTraits);

                championRepository.save(db);
            }
        }
    }

    private static String key(String set, String name) {
        return set + "::" + name;
    }
}
