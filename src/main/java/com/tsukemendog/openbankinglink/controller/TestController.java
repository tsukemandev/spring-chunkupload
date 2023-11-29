package com.tsukemendog.openbankinglink.controller;

import com.tsukemendog.openbankinglink.dto.TestDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/test")
    public Map<String, Object> greeting(@RequestParam("testID") String testId, @RequestBody TestDto testDto) {

        return Collections.singletonMap("message", testDto.getKey1() + " " + testDto.getKey2());
    }


}
