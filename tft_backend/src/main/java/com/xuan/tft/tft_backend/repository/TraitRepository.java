package com.xuan.tft.tft_backend.repository;

import com.xuan.tft.tft_backend.entity.Trait;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TraitRepository extends JpaRepository<Trait, Long> {

    Trait findByName(String name);
}
