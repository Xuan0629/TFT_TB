package com.xuan.tft.tft_backend.controller.ChampionController;

import com.xuan.tft.tft_backend.entity.Champion;
import com.xuan.tft.tft_backend.repository.ChampionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/champions")
public class ChampionController {

    private final ChampionRepository championRepository;

    public ChampionController(ChampionRepository championRepository) {
        this.championRepository = championRepository;
    }

    // GET /api/champions  → 返回所有棋子
    @GetMapping
    public List<Champion> getAllChampions() {
        return championRepository.findAll();
    }

    // 后面可以加：
    // GET /api/champions/{id}
    // POST /api/champions
    // 等等
}
