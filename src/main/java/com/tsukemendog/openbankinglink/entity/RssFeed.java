package com.tsukemendog.openbankinglink.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RssFeed {

    @Id
    private String code;

    @Column(columnDefinition = "TEXT")
    private String content;
}
