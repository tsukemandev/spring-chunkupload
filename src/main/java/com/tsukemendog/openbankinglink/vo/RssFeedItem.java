package com.tsukemendog.openbankinglink.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@AllArgsConstructor
@Builder
public class RssFeedItem {

    private final String title;
    private final String description;
    private final String author;
    private final String link;
    private final Boolean parmaLink;
    private final String pubDate;
    private final String comments;
    private final List<String> category;
    private final String guid;
    private final String channelTitle;

}
