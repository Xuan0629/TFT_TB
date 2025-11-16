package com.xuan.tft.tft_backend.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "champions")
public class Champion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 棋子名称
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // 费用（1~5 费）
    @Column(nullable = false)
    private Integer cost;

    // 多对多
    @ManyToMany
    @JoinTable(
            name = "champion_traits",
            joinColumns = @JoinColumn(name = "champion_id"),
            inverseJoinColumns = @JoinColumn(name = "trait_id")
    )
    private Set<Trait> traits = new HashSet<>();

    // TODO:羁绊、多对多关联
    // private Set<Trait> traits;

    public Champion() {
    }

    public Champion(String name, Integer cost) {
        this.name = name;
        this.cost = cost;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCost() {
        return cost;
    }

    public Set<Trait> getTraits() {
        return traits;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public void setTraits(Set<Trait> traits) {
        this.traits = traits;
    }
}

