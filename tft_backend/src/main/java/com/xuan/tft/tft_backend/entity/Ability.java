package com.xuan.tft.tft_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "abilities",
        uniqueConstraints = @UniqueConstraint(name = "uk_ability_name", columnNames = {"name"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Ability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length = 100)
    private String name;

//    @Column(columnDefinition = "TEXT")
//    private String description;

    // --- getters & setters ---
    public Long getId() { return id; }
    public String getName() { return name; }
//    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
//    public void setDescription(String description) { this.description = description; }
}
