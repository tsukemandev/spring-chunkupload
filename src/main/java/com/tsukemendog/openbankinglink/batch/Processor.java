package com.tsukemendog.openbankinglink.batch;

import org.springframework.batch.item.ItemProcessor;

public class Processor implements ItemProcessor<String, String> {
    @Override
    public String process(String data) throws Exception {
        System.out.println("process : " + data);
        return data.toUpperCase();
    }
}
