package com.xuan.tft.tft_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "roles",
        uniqueConstraints = @UniqueConstraint(name = "uk_role_name", columnNames = {"name"}))
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length = 64)
    private String name;

//    @Column(length = 255)
//    private String description;

    // --- getters & setters ---
    public Long getId() { return id; }
    public String getName() { return name; }
//    public String getDescription() { return description; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
//    public void setDescription(String description) { this.description = description; }
}
