package com.xuan.tft.tft_backend.dto;

import java.util.List;

public class ChampionBasicDto {

    private Long id;
    private String name;
    private Integer cost;
    private List<String> traits;

    public ChampionBasicDto() {
    }

    public ChampionBasicDto(Long id, String name, Integer cost, List<String> traits) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.traits = traits;
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

    public List<String> getTraits() {
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

    public void setTraits(List<String> traits) {
        this.traits = traits;
    }
}
