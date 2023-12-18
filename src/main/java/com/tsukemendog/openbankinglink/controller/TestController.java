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
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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

    @PostMapping("/upload")
    public ResponseEntity<?> uploadChunk(
            @RequestHeader("Chunk-Start") Long chunkStart,
            @RequestHeader("Chunk-End") Long chunkEnd,
            @RequestHeader("File-Name") String fileName,
            @RequestBody byte[] chunkData) {

        System.out.println("chucnk : " + fileName + "    byte : " + new String(chunkData, StandardCharsets.UTF_8));
        
        // 청크 데이터 처리 로직 구현
        // 예: 파일 시스템에 청크 저장, 청크 재조합 등

        return ResponseEntity.ok().build();
    }

    @PostMapping("/upload2")
    public ResponseEntity<String> fileUploadTest2(@RequestParam("file") MultipartFile multipartFile) {

        System.out.println("file size : " + multipartFile.getSize());

        return ResponseEntity.ok("ok");
    }

}
