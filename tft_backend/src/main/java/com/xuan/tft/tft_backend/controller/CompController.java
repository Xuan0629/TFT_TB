package com.xuan.tft.tft_backend.controller;

import com.xuan.tft.tft_backend.dto.CompCreateRequest;
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
    public ResponseEntity<Comp> createComp(@RequestBody CompCreateRequest request) {
        Comp created = compService.createComp(request);
        return ResponseEntity.ok(created);
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
}
