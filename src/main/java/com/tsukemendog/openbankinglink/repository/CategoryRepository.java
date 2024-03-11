package com.tsukemendog.openbankinglink.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.tsukemendog.openbankinglink.entity.Category;

public interface CategoryRepository extends CrudRepository<Category, Long>{
    List<Category> findAllByOrderByOrdNumAsc();

    @EntityGraph(attributePaths = "videos")
    Optional<Category> findByType(String type);

    @Query("select c from Category c join c.videos v where c.type = :type and v.id = :videoId")
    Optional<Category> fintByTypeAndVideoId(@Param("type") String type, @Param("videoId") Long videoId);
}
