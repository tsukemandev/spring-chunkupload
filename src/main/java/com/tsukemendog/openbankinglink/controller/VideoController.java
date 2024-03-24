package com.tsukemendog.openbankinglink.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.tsukemendog.openbankinglink.entity.Category;
import com.tsukemendog.openbankinglink.entity.Video;
import com.tsukemendog.openbankinglink.repository.CategoryRepository;
import com.tsukemendog.openbankinglink.repository.VideoRepository;

@RestController
public class VideoController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private VideoRepository videoRepository;
    
    @GetMapping("/category")
    public ResponseEntity<Map<String, Object>> getCategory() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", categoryRepository.findAllByOrderByOrdNumAsc());
        return ResponseEntity.ok(resultMap);
    }

    @GetMapping("/video/{category}")
    public ResponseEntity<Map<String, Object>> getVideos(@PathVariable("category") String categoryType) {
        Map<String, Object> resultMap = new HashMap<>();

        Optional<Category> cOptional = categoryRepository.findByType(categoryType);
        if (!cOptional.isPresent()) {
            return ResponseEntity.badRequest().body(resultMap);
        }

        resultMap.put("data", cOptional.get().getVideos());
        return ResponseEntity.ok(resultMap);
    }

    @GetMapping("/video/{category}/{videoId}")
    public ResponseEntity<Map<String, Object>> getVideo(@PathVariable("category") String categoryType, @PathVariable String videoId) {
        Map<String, Object> resultMap = new HashMap<>();

        if (videoId == null || !videoId.matches("^\\d+$")) {
            return ResponseEntity.badRequest().body(resultMap);
        }

        Optional<Video> vOptional = videoRepository.fintByTypeAndVideoId(categoryType, Long.parseLong(videoId));
        if (!vOptional.isPresent()) {
            return ResponseEntity.badRequest().body(resultMap);
        } 

        resultMap.put("data", vOptional.get());
        return ResponseEntity.ok(resultMap);
    }
}
