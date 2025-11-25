package com.xuan.tft.tft_backend.repository;

import com.xuan.tft.tft_backend.entity.Ability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AbilityRepository extends JpaRepository<Ability, Long> {
    Optional<Ability> findByName(String name);
}
