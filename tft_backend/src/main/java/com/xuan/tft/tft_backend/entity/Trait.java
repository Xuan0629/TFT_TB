package com.xuan.tft.tft_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "traits")
public class Trait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 羁绊名称
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // 简单描述
    @Column(length = 255)
    private String description;

    public Trait() {
    }

    public Trait(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // getter / setter

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
