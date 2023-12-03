package com.tsukemendog.openbankinglink.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private PlatformTransactionManager platformTransactionManager;


    @Autowired
    private StepBuilderFactory steps;

    @Autowired
    private JobBuilderFactory jobs;


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
    public Job job(@Qualifier("step1") Step step1) {
        return jobs.get("myJob").start(step1).build();
    }

    @Bean
    protected Step step1() {
        return steps.get("step1")
                .<String, String> chunk(10)
                .reader(new Reader())
                .processor(new Processor())
                .writer(new Writer())
                .build();
    }



    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionListener();
    }

}
