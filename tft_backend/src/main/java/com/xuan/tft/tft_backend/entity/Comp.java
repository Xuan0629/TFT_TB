package com.xuan.tft.tft_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comps")
public class Comp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 阵容名称
    @Column(nullable = false, length = 100)
    private String name;

    // 简单描述：思路、站位等
    @Column(length = 500)
    private String description;

    // 评分，使用算法计算
    @Column(nullable = false)
    private Integer score;

    // 时间戳（自动填充）
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 人气指标：被点赞/收藏次数
    @Column(nullable = false)
    private Integer likes = 0;

    // 使用次数（被预览/使用的统计）
    @Column(nullable = false)
    private Integer usageCount = 0;

    // 创建该阵容的用户
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    // 多对多
    @ManyToMany
    @JoinTable(
            name = "comp_champions",
            joinColumns = @JoinColumn(name = "comp_id"),
            inverseJoinColumns = @JoinColumn(name = "champion_id")
    )
    private Set<Champion> champions = new HashSet<>();

    public Comp() {
    }

    public Comp(String name, String description, Integer score) {
        this.name = name;
        this.description = description;
        this.score = score;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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

    public Integer getScore() {
        return score;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<Champion> getChampions() {
        return champions;
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

    public void setChampions(Set<Champion> champions) {
        this.champions = champions;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }


}
