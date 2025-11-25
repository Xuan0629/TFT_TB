package com.xuan.tft.tft_backend.repository;

import com.xuan.tft.tft_backend.entity.Champion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface ChampionRepository extends JpaRepository<Champion, Long> {

    Optional<Champion> findBySetNameAndName(String setName, String name);

    long countBySetName(String setName);

    Optional<Champion> findByName(String name);

    void deleteBySetName(String setName);
}
