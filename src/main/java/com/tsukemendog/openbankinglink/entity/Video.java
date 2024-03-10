package com.tsukemendog.openbankinglink.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Video {
    
    @Id
    @GeneratedValue
    private Long id;

    private String name;  //이름

    private String type;  //movie, anime

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
