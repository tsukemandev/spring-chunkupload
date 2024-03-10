package com.tsukemendog.openbankinglink.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Category {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private Long ordNum;
    private LocalDateTime created;
    private LocalDateTime modified;

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<Video> videos;

    public Category(String name, String type, Long ordNum) {
        this.name = name;
        this.type = type;
        this.ordNum = ordNum;
    }

    @PrePersist
    private void onCreate() {
        this.created = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        this.modified = LocalDateTime.now();
    }

}
