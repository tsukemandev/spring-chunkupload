package com.tsukemendog.openbankinglink.batch;

import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

//https://www.javainuse.com/spring/bootbatch 스프링 배치 간략설명
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Value("file:text.txt")
    private Resource outputTxt;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Bean
    public Reader itemReader()
            throws UnexpectedInputException, ParseException {
        return new Reader();
    }

    @Bean
    public Processor itemProcessor()
            throws UnexpectedInputException, ParseException {
        return new Processor();
    }
    @Bean
    public Writer itemWriter()
            throws UnexpectedInputException, ParseException {
        return new Writer();
    }


    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionListener();
    }

}
