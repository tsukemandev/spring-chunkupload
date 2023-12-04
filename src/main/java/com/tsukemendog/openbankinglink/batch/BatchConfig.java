package com.tsukemendog.openbankinglink.batch;

import com.tsukemendog.openbankinglink.entity.RssFeed;
import com.tsukemendog.openbankinglink.vo.RssFeedItem;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

//https://www.javainuse.com/spring/bootbatch 스프링 배치 간략설명
@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Value("file:../../../../../resources/test/text.txt")
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
    public Writer itemJpaWriter()
            throws UnexpectedInputException, ParseException {
        return new Writer();
    }
    @Bean
    public FlatFileItemWriter<String> itemFileWriter() {
        FlatFileItemWriter<String> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource("test/text.txt")); // 출력 파일 경로 설정

        // 각 항목을 한 줄에 쓰기 위해 설정
        writer.setLineAggregator(new PassThroughLineAggregator<>());

        return writer;
    }

    @Bean
    public Job job(@Qualifier("step1") Step step1) {
        return jobs.get("myJob").start(step1).build();
    }

    @Bean
    protected Step step1() {
        return steps.get("step1")
                .<List<RssFeedItem>, RssFeed> chunk(1)
                .reader(itemReader())
                .processor(itemProcessor())
                .writer(itemJpaWriter())
                .build();
    }



    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionListener();
    }

}
