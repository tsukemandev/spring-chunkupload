package com.tsukemendog.openbankinglink.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.tsukemendog.openbankinglink.entity.RssFeed;
import com.tsukemendog.openbankinglink.repository.RssFeedRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

@RestController
public class RssController {

    @Autowired
    private RssFeedRepository rssFeedRepository;
    
    @GetMapping("/rss/{code}")
    public ResponseEntity<String> getFeed(@PathVariable String code) {
        Optional<RssFeed> rssFeed = Optional.empty();
        if ("movie".equals(code)) {
            rssFeed = rssFeedRepository.findByCode("movie");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(rssFeed.map(RssFeed::getContent).orElse("Empty"), headers, HttpStatus.OK);
    }
}
