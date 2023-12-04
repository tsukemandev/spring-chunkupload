package com.tsukemendog.openbankinglink.batch;

import com.tsukemendog.openbankinglink.entity.RssFeed;
import com.tsukemendog.openbankinglink.repository.RssFeedRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class Writer implements ItemWriter<RssFeed> {

    @Autowired
    private RssFeedRepository rssFeedRepository;
    @Override
    public void write(List<? extends RssFeed> items) throws Exception {
        rssFeedRepository.saveAll(items);
        System.out.println("입력완료!");
    }
}
