package com.xuan.tft.tft_backend.dto;

import java.util.List;

public class CompCreateRequest {

    private String name;
    private String description;
    private List<Long> championIds;
//    private Long userId;

    public CompCreateRequest() {
    }

    public CompCreateRequest(String name, String description, List<Long> championIds, Long userId) {
        this.name = name;
        this.description = description;
        this.championIds = championIds;
//        this.userId = userId;
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

//    public Long getUserId(){
//        return userId;
//    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setChampionIds(List<Long> championIds) {
        this.championIds = championIds;
    }

//    public void setUserId(Long userId){
//        this.userId = userId;
//    }
}
