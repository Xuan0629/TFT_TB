package com.xuan.tft.tft_backend.repository;

import com.xuan.tft.tft_backend.entity.Trait;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TraitRepository extends JpaRepository<Trait, Long> {

    Optional<Trait> findBySetNameAndName(String setName, String name);

    long countBySetName(String setName);

    Optional<Trait> findByName(String name);

    void deleteBySetName(String setName);
}
