package com.tsukemendog.openbankinglink.batch;

import com.apptasticsoftware.rssreader.RssReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsukemendog.openbankinglink.entity.RssFeed;
import com.tsukemendog.openbankinglink.vo.RssFeedItem;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Log4j2
public class Reader implements ItemReader<List<RssFeedItem>> {
    private boolean isComplete = false;

    @Override
    public List<RssFeedItem> read() throws Exception {
        if (!isComplete) {
            isComplete = true;
            return getRssFeedItems();
        }
        return null;
    }

    public List<RssFeedItem> getRssFeedItems() throws IOException {
        List<RssFeedItem> list = new ArrayList<>();
        RssReader rssReader = new RssReader();

        log.info("===================================================");
        rssReader.read("https://screenrant.com/feed/movie-news/").collect(Collectors.toList()).forEach(el -> {

            String pubDate = "";
            LocalDateTime givenDateTime = getPubLocalDate(el.getPubDate().orElse(""));

            if (givenDateTime != null) {
                // 현재의 GMT 기준 시각을 가져오기
                Instant currentGMT = Instant.now();

                // GMT 기준 시각을 사용하여 LocalDateTime 객체로 변환
                LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentGMT, ZoneId.of("GMT"));

                //주어진 LocalDateTime 객체와 현재 시간 사이의 차이 (분 단위)
                long minutesAgo = ChronoUnit.MINUTES.between(givenDateTime, currentDateTime);

                if (minutesAgo < 60) {
                    pubDate = minutesAgo + "분전";
                } else if (minutesAgo / 60 < 24) {
                    pubDate = (minutesAgo / 60) + "시간전";
                } else if (minutesAgo / 60 >= 24) {
                    pubDate = (minutesAgo / 60) / 24 + "일전";
                }
            }

            list.add(RssFeedItem.builder()
                    .title(el.getTitle().orElse(""))
                    .description(el.getDescription().orElse(""))
                    .author(el.getAuthor().orElse(""))
                    .link(el.getLink().orElse(""))
                    .parmaLink(el.getIsPermaLink().orElse(false))
                    .pubDate(pubDate)
                    .comments(el.getComments().orElse(""))
                    .category(el.getCategories())
                    .guid(el.getGuid().orElse(""))
                    .channelTitle(el.getChannel().getTitle())
                    .build());


        });

        list.forEach(o ->  System.out.println("rss item : " + o.getTitle()));

        return list;

    }

    public LocalDateTime getPubLocalDate(String dateString) {
        if ("".equals(dateString)) {
            return null;
        }
        // DateTimeFormatter 정의 (주어진 날짜 형식에 맞춰야 함)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

        // 문자열을 ZonedDateTime으로 파싱
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, formatter);

        // ZonedDateTime을 LocalDateTime으로 변환
        return zonedDateTime.toLocalDateTime();

    }
}
