package com.xuan.tft.tft_backend.controller;

import com.xuan.tft.tft_backend.dto.CompCreateRequest;
import com.xuan.tft.tft_backend.dto.CompSummaryDto;
import com.xuan.tft.tft_backend.entity.Comp;
import com.xuan.tft.tft_backend.service.CompService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comps")
public class CompController {

    private final CompService compService;

    public CompController(CompService compService) {
        this.compService = compService;
    }

    // 创建阵容：POST /api/comps
    @PostMapping
    public ResponseEntity<CompSummaryDto> createComp(@RequestBody CompCreateRequest request) {
        CompSummaryDto created = compService.createComp(request);
        return ResponseEntity.ok(created);
    }

    // 预览阵容（不保存，仅返回评分和羁绊信息）
    @PostMapping("/preview")
    public ResponseEntity<CompSummaryDto> previewComp(@RequestBody CompCreateRequest request) {
        CompSummaryDto preview = compService.previewComp(request);
        return ResponseEntity.ok(preview);
    }

    // 查询所有阵容：GET /api/comps
    @GetMapping
    public List<Comp> getAllComps() {
        return compService.getAllComps();
    }

    // 按 ID 查询：GET /api/comps/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Comp> getCompById(@PathVariable Long id) {
        return compService.getCompById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 按羁绊搜索阵容：GET /api/comps/search?trait=Mage
    @GetMapping("/search")
    public ResponseEntity<List<CompSummaryDto>> searchByTrait(@RequestParam("trait") String traitName) {
        List<CompSummaryDto> result = compService.searchByTrait(traitName);
        return ResponseEntity.ok(result);
    }

    // 热门阵容：GET /api/comps/top?limit=5
    @GetMapping("/top")
    public ResponseEntity<List<CompSummaryDto>> getTopComps(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        List<CompSummaryDto> result = compService.getTopComps(limit);
        return ResponseEntity.ok(result);
    }

    // 为某个阵容点赞：POST /api/comps/{id}/like
    @PostMapping("/{id}/like")
    public ResponseEntity<CompSummaryDto> likeComp(@PathVariable Long id) {
        CompSummaryDto updated = compService.likeComp(id);
        return ResponseEntity.ok(updated);
    }

    // 记录某个阵容被使用：POST /api/comps/{id}/use
    @PostMapping("/{id}/use")
    public ResponseEntity<Void> useComp(@PathVariable Long id) {
        compService.increaseUsage(id);
        return ResponseEntity.noContent().build();
    }

}
