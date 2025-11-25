package com.xuan.tft.tft_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "champions",
        uniqueConstraints = @UniqueConstraint(name = "uk_champion_set_name", columnNames = {"set_name", "name"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Champion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="set_name", nullable = false, length = 16)
    private String setName; // S15

    @Column(nullable = false, length = 100)
    private String name;    // 中文名

    @Column(nullable = false)
    private Integer cost = 1;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "champion_traits",
            joinColumns = @JoinColumn(name = "champion_id"),
            inverseJoinColumns = @JoinColumn(name = "trait_id"))
    private Set<Trait> traits = new HashSet<>();

    // 新增：角色（多对一）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    // 新增：技能/团队能力（多对一）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ability_id")
    private Ability ability;

    // --- getters & setters ---

    public Long getId() { return id; }
    public String getSetName() { return setName; }
    public String getName() { return name; }
    public Integer getCost() { return cost; }
    public Set<Trait> getTraits() { return traits; }
    public Role getRole() { return role; }
    public Ability getAbility() { return ability; }

    public void setId(Long id) { this.id = id; }
    public void setSetName(String setName) { this.setName = setName; }
    public void setName(String name) { this.name = name; }
    public void setCost(Integer cost) { this.cost = cost; }
    public void setTraits(Set<Trait> traits) { this.traits = traits; }
    public void setRole(Role role) { this.role = role; }
    public void setAbility(Ability ability) { this.ability = ability; }
}
