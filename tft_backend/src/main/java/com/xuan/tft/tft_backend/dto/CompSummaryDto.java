package com.xuan.tft.tft_backend.dto;

import java.util.List;

public class CompSummaryDto {

    private Long id; // 预览时可以为 null
    private String name;
    private String description;
    private Integer score;

    private List<ChampionBasicDto> champions;
    private List<TraitCountDto> traits;

    public CompSummaryDto() {
    }

    public CompSummaryDto(Long id,
                          String name,
                          String description,
                          Integer score,
                          List<ChampionBasicDto> champions,
                          List<TraitCountDto> traits) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.score = score;
        this.champions = champions;
        this.traits = traits;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getScore() {
        return score;
    }

    public List<ChampionBasicDto> getChampions() {
        return champions;
    }

    public List<TraitCountDto> getTraits() {
        return traits;
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

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setChampions(List<ChampionBasicDto> champions) {
        this.champions = champions;
    }

    public void setTraits(List<TraitCountDto> traits) {
        this.traits = traits;
    }
}
