package com.tsukemendog.openbankinglink.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tsukemendog.openbankinglink.entity.Category;
import com.tsukemendog.openbankinglink.entity.Video;

public interface VideoRepository extends CrudRepository<Video, Long>{

    //@Query("select c from Category c join c.videos v where c.type = :type and v.id = :videoId")
    @Query("select v from Video v join v.category c where c.type = :type and v.id = :videoId")
    Optional<Video> fintByTypeAndVideoId(@Param("type") String type, @Param("videoId") Long videoId);
}
