package com.xuan.tft.tft_backend.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "traits",
        uniqueConstraints = @UniqueConstraint(name = "uk_trait_set_name", columnNames = {"set_name", "name"}))
public class Trait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="set_name", nullable = false, length = 16)
    private String setName; // S15 / S16 ...

    @Column(nullable = false, length = 100)
    private String name;    // zh_CN

    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "trait_levels", joinColumns = @JoinColumn(name = "trait_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_trait_level_unique",
                    columnNames = {"trait_id", "count_required"}))
    @Column(name = "count_required", nullable = false)
    private List<Integer> levels = new ArrayList<>();

    // --- getters & setters ---
    public Long getId() { return id; }
    public String getSetName() { return setName; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<Integer> getLevels() { return levels; }

    public void setId(Long id) { this.id = id; }
    public void setSetName(String setName) { this.setName = setName; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLevels(List<Integer> levels) { this.levels = levels; }
}
