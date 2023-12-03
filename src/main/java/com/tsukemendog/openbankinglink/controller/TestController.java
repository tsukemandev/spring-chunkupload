package com.tsukemendog.openbankinglink.controller;


import com.tsukemendog.openbankinglink.dto.TestDto;
import com.tsukemendog.openbankinglink.entity.RssFeed;
import com.tsukemendog.openbankinglink.repository.RssFeedRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
public class TestController {

    @Autowired
    private RssFeedRepository rssFeedRepository;


    @Autowired
    private Job job;

    @Autowired
    private JobLauncher jobLauncher;

    @GetMapping("/test")
    public Map<String, Object> greeting(@RequestParam("testID") String testId, @RequestBody TestDto testDto) {

        return Collections.singletonMap("message", testDto.getKey1() + " " + testDto.getKey2());
    }
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

    @GetMapping("/invokejob")
    public String handle() throws Exception {
        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("time", new JobParameter(System.currentTimeMillis()));
        jobLauncher.run(job, new JobParameters(confMap));
        return "Batch job has been invoked";
    }

}
