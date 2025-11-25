package com.xuan.tft.tft_backend.service.importer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class CdragonParser {

    public record RawTrait(String setName, String name, String description, List<Integer> levels) {}
    public record RawChampion(String setName, String name, Integer cost, List<String> traitNames,
                              String roleName, String abilityName) {}
    public record RawSet(List<RawTrait> traits, List<RawChampion> champions) {}

    private final ObjectMapper mapper = new ObjectMapper();
    private static final Pattern CHARACTER_ICON = Pattern.compile("ASSETS/Characters/.+?/HUD/.+");
    private final String setName;

    public CdragonParser(String setName) { this.setName = setName; }

    public RawSet parseCdragon(InputStream is) throws Exception {
        JsonNode root = mapper.readTree(is);

        if (root.isArray()) {
            List<Map<String, Object>> list = mapper.convertValue(root, new TypeReference<>() {});
            return parseFromFlatArray(list);
        }

        if (root.isObject()) {
            if (root.has("traits") || root.has("units") || root.has("champions")) {
                List<Map<String,Object>> traits = root.has("traits")
                        ? mapper.convertValue(root.get("traits"), new TypeReference<>() {})
                        : Collections.emptyList();
                List<Map<String,Object>> units = root.has("units")
                        ? mapper.convertValue(root.get("units"), new TypeReference<>() {})
                        : (root.has("champions")
                        ? mapper.convertValue(root.get("champions"), new TypeReference<>() {})
                        : Collections.emptyList());
                return parseFromStructuredObject(traits, units);
            }
            throw new IllegalArgumentException(
                    "检测到传入的是对象(Object)但不包含 traits[]/units[]/champions[]。请提供包含静态赛季数据的 JSON。");
        }

        throw new IllegalArgumentException("无法识别的 JSON 根节点类型（既不是数组也不是对象）");
    }

    private RawSet parseFromFlatArray(List<Map<String, Object>> list) {
        List<RawTrait> traits = new ArrayList<>();
        List<RawChampion> champions = new ArrayList<>();
        Set<String> traitNameCandidates = new LinkedHashSet<>();

        for (Map<String, Object> node : list) {
            String name = asString(node.get("name"));
            String icon = asString(node.get("icon"));
            String desc = sanitizeDesc(asString(node.get("desc")));
            List<String> associatedTraits = asStringList(node.get("associatedTraits"));
            List<String> tags = asStringList(node.get("tags"));

            if (associatedTraits != null) traitNameCandidates.addAll(associatedTraits);
            if (tags != null) for (String t : tags) if (!isHashLike(t)) traitNameCandidates.add(t);

            if (icon != null && CHARACTER_ICON.matcher(icon).find() && name != null && !name.isBlank()) {
                champions.add(new RawChampion(setName, name, 1, Collections.emptyList(), null, null));
            }
        }
        for (String tName : traitNameCandidates) {
            if (tName != null && !tName.isBlank())
                traits.add(new RawTrait(setName, tName, null, Collections.emptyList()));
        }
        return new RawSet(traits, champions);
    }

    private RawSet parseFromStructuredObject(List<Map<String,Object>> traitsNode,
                                             List<Map<String,Object>> unitsNode) {
        List<RawTrait> traits = new ArrayList<>();
        List<RawChampion> champions = new ArrayList<>();
        Map<String, RawTrait> traitMapByName = new LinkedHashMap<>();

        for (Map<String,Object> t : traitsNode) {
            String name = asString(t.getOrDefault("name", t.get("displayName")));
            String desc = sanitizeDesc(asString(t.get("desc")));
            List<Integer> levels = asIntList(t.get("levels"));
            if (name != null && !name.isBlank()) {
                RawTrait rt = new RawTrait(setName, name, desc, levels == null ? new ArrayList<>() : levels);
                traits.add(rt);
                traitMapByName.put(name, rt);
            }
        }

        for (Map<String,Object> u : unitsNode) {
            String name = asString(u.getOrDefault("name", u.get("displayName")));
            Integer cost = asInt(u.get("cost"), 1);
            List<String> traitNames = new ArrayList<>();
            String roleName = asString(u.get("role"));
            String abilityName = asString(u.get("ability"));

            Object tr = u.get("traits");
            if (tr instanceof List<?> l) {
                for (Object o : l) {
                    if (o == null) continue;
                    String s = String.valueOf(o);
                    traitNames.add(s);
                    traitMapByName.putIfAbsent(s, new RawTrait(setName, s, null, Collections.emptyList()));
                }
            }

            if (name != null && !name.isBlank()) {
                champions.add(new RawChampion(setName, name, cost, traitNames, roleName, abilityName));
            }
        }

        List<RawTrait> mergedTraits = new ArrayList<>(traitMapByName.values());
        return new RawSet(mergedTraits, champions);
    }

    private static String asString(Object v) { return v == null ? null : String.valueOf(v); }
    private static Integer asInt(Object v, int def) {
        if (v == null) return def;
        try { if (v instanceof Number n) return n.intValue(); return Integer.parseInt(String.valueOf(v)); }
        catch (Exception e) { return def; }
    }
    private static List<Integer> asIntList(Object v) {
        if (v == null) return new ArrayList<>();
        List<Integer> out = new ArrayList<>();
        if (v instanceof List<?> l) {
            for (Object o : l) {
                try {
                    if (o instanceof Number n) out.add(n.intValue());
                    else out.add(Integer.parseInt(String.valueOf(o)));
                } catch (Exception ignored) {}
            }
        }
        return out;
    }
    private static boolean isHashLike(String s) {
        return s != null && s.startsWith("{") && s.endsWith("}") && s.length() > 2;
    }
    private static List<String> asStringList(Object v) {
        if (v == null) return Collections.emptyList();
        if (v instanceof List<?> l) {
            List<String> out = new ArrayList<>();
            for (Object o : l) if (o != null) out.add(String.valueOf(o));
            return out;
        }
        return Collections.emptyList();
    }
    private static String sanitizeDesc(String htmlOrRaw) {
        if (htmlOrRaw == null) return null;
        return htmlOrRaw.replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n");
    }
}
