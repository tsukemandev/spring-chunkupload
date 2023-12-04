package com.tsukemendog.openbankinglink.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsukemendog.openbankinglink.entity.RssFeed;
import com.tsukemendog.openbankinglink.vo.RssFeedItem;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class Processor implements ItemProcessor<List<RssFeedItem>, RssFeed> {
    @Override
    public RssFeed process(List<RssFeedItem> data) throws Exception {
        System.out.println("process : " + data);
        ObjectMapper objectMapper = new ObjectMapper();
        return RssFeed.builder()
                .code("movie")
                .content(objectMapper.writeValueAsString(data))
                .build();
    }
}
