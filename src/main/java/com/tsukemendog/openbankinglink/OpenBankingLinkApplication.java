package com.tsukemendog.openbankinglink;

import com.apptasticsoftware.rssreader.Channel;
import com.apptasticsoftware.rssreader.Image;
import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsukemendog.openbankinglink.entity.Customer;
import com.tsukemendog.openbankinglink.entity.RssFeed;
import com.tsukemendog.openbankinglink.repository.CustomerRepository;
import com.tsukemendog.openbankinglink.repository.RssFeedRepository;
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
    public CommandLineRunner demo(CustomerRepository repository, RssFeedRepository rssFeedRepository) {
        return (args) -> {
            // save a few customers
            repository.save(new Customer("Jack", "Bauer"));
            repository.save(new Customer("Chloe", "O'Brian"));
            repository.save(new Customer("Kim", "Bauer"));
            repository.save(new Customer("David", "Palmer"));
            repository.save(new Customer("Michelle", "Dessler"));

            // fetch all customers
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            repository.findAll().forEach(customer -> {
                log.info(customer.toString());
            });
            log.info("");

            // fetch an individual customer by ID
            Customer customer = repository.findById(1L);
            log.info("Customer found with findById(1L):");
            log.info("--------------------------------");
            log.info(customer.toString());
            log.info("");

            // fetch customers by last name
            log.info("Customer found with findByLastName('Bauer'):");
            log.info("--------------------------------------------");
            repository.findByLastName("Bauer").forEach(bauer -> {
                log.info(bauer.toString());
            });


            var list = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            RssReader rssReader = new RssReader();

            log.info("===================================================");
            rssReader.read("https://screenrant.com/feed/movie-news/").collect(Collectors.toList()).forEach(el -> {

                String pubDate;
                LocalDateTime givenDateTime = getPubLocalDate(el.getPubDate().orElse(""));

                if (givenDateTime == null) {
                    pubDate = "";
                } else {

                    // 현재의 GMT 기준 시각을 가져오기
                    Instant currentGMT = Instant.now();

                    // GMT 기준 시각을 사용하여 LocalDateTime 객체로 변환
                    LocalDateTime currentDateTime = LocalDateTime.ofInstant(currentGMT, ZoneId.of("GMT"));

                    //주어진 LocalDateTime 객체와 현재 시간 사이의 차이 (분 단위)
                    long minutesAgo = ChronoUnit.MINUTES.between(givenDateTime, currentDateTime);
                }

                list.add(RssFeedItem.builder()
                                .title(el.getTitle().orElse(""))
                                .description(el.getDescription().orElse(""))
                                .author(el.getAuthor().orElse(""))
                                .link(el.getLink().orElse(""))
                                .parmaLink(el.getIsPermaLink().orElse(false))
                                .pubDate(el.getPubDate().orElse(""))
                                .comments(el.getComments().orElse(""))
                                .category(el.getCategories())
                                .guid(el.getGuid().orElse(""))
                                .channelTitle(el.getChannel().getTitle())
                        .build());


            });





            log.info("RSS Feed 획득 완료");
            log.info("===================================================");

            rssFeedRepository.save(RssFeed.builder()
                            .code("movie")
                            .content(objectMapper.writeValueAsString(list))
                    .build());
        };
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
