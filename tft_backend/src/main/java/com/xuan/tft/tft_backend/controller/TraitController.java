package com.xuan.tft.tft_backend.controller;

import com.xuan.tft.tft_backend.entity.Trait;
import com.xuan.tft.tft_backend.repository.TraitRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traits")
public class TraitController {

    private final TraitRepository traitRepository;

    public TraitController(TraitRepository traitRepository) {
        this.traitRepository = traitRepository;
    }

    @GetMapping
    public List<Trait> getAllTraits() {
        return traitRepository.findAll();
    }
}
