package com.xuan.tft.tft_backend.dto;

import java.util.List;

public class CompCreateRequest {

    private String name;
    private String description;
    private List<Long> championIds;

    public CompCreateRequest() {
    }

    public CompCreateRequest(String name, String description, List<Long> championIds) {
        this.name = name;
        this.description = description;
        this.championIds = championIds;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Long> getChampionIds() {
        return championIds;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setChampionIds(List<Long> championIds) {
        this.championIds = championIds;
    }
}
