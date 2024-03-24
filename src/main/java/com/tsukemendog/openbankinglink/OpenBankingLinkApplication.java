package com.tsukemendog.openbankinglink;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Image;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsukemendog.openbankinglink.entity.Category;
import com.tsukemendog.openbankinglink.entity.Customer;
import com.tsukemendog.openbankinglink.entity.RssFeed;
import com.tsukemendog.openbankinglink.entity.Video;
import com.tsukemendog.openbankinglink.repository.CategoryRepository;
import com.tsukemendog.openbankinglink.repository.CustomerRepository;
import com.tsukemendog.openbankinglink.repository.RssFeedRepository;
import com.tsukemendog.openbankinglink.repository.VideoRepository;
import com.tsukemendog.openbankinglink.vo.RssFeedItem;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

@Log4j2
@SpringBootApplication
public class OpenBankingLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenBankingLinkApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            log.info("current imported properties profile : " + environment.getProperty("current-profile"));
        };
    }

    //https://openjdk.org/groups/net/httpclient/recipes.html httpclient 레시피
    @Bean
    @Profile("dev")
    public CommandLineRunner demo(CustomerRepository repository, RssFeedRepository rssFeedRepository, CategoryRepository categoryRepository, VideoRepository videoRepository) {
        return (args) -> {
   
            Category category1 = categoryRepository.save(new Category("영화", "movie", 1L));
            Category category2 = categoryRepository.save(new Category("애니메이션", "anime", 2L));

            videoRepository.save(Video.builder().title("테스트 영화1").url("https://d1buzuj0s6bbq3.cloudfront.net/hungry-days-bump-of-chicken.m3u8").category(category1).thumbnail("https://d1buzuj0s6bbq3.cloudfront.net/thumbnail/onepiece-noodle/b-33.jpg").build());
            videoRepository.save(Video.builder().title("테스트 영화2").url("https://d1buzuj0s6bbq3.cloudfront.net/hungry-days-bump-of-chicken.m3u8").category(category1).thumbnail("https://d1buzuj0s6bbq3.cloudfront.net/thumbnail/onepiece-noodle/b-33.jpg").build());
            videoRepository.save(Video.builder().title("테스트 애니메1").url("https://d1buzuj0s6bbq3.cloudfront.net/hungry-days-bump-of-chicken.m3u8").category(category2).thumbnail("https://d1buzuj0s6bbq3.cloudfront.net/thumbnail/onepiece-noodle/b-33.jpg").build());

        };
    }

}
