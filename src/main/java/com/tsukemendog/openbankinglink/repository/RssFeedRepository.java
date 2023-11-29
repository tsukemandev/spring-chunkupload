package com.tsukemendog.openbankinglink.repository;

import com.tsukemendog.openbankinglink.entity.Customer;
import com.tsukemendog.openbankinglink.entity.RssFeed;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RssFeedRepository extends CrudRepository<RssFeed, String> {
    Optional<RssFeed> findByCode(String code);
}
