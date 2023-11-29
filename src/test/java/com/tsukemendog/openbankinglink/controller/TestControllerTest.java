package com.tsukemendog.openbankinglink.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tsukemendog.openbankinglink.security.WebSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;


import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import static com.tsukemendog.openbankinglink.ApiDocumentUtils.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest({TestController.class, WebSecurityConfig.class})
@AutoConfigureRestDocs(uriScheme = "http", uriHost = "docs.api-skyclassism.com")
public class TestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;
/*
    @BeforeEach
    void setup() { //text/html;charset=UTF-8
        this.mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON).param("testIdd","ㅋㅋㅋ"))
                .alwaysExpect(status().isOk())
                .build();
    }
*/


    @Test
    void test() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");


        ResultActions result = this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/test")
                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                        .param("testID", "HELLO!!!").content(objectMapper.writeValueAsString(map)));

                result.andExpect(status().isOk()).andDo(document("my-ascii", // (4)
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("testID"). description("wwwwwwww")
                        ),
                        requestFields(
                                fieldWithPath("key1").type(JsonFieldType.STRING).description("ㅎㅇㅎㅇ"),
                                fieldWithPath("key2").type(JsonFieldType.STRING).description("ㅎㅇㅎㅇ"),
                                fieldWithPath("key3").type(JsonFieldType.STRING).description("ㅎㅇㅎㅇ")
                        ),
                        responseFields(fieldWithPath("message").type(JsonFieldType.STRING).description("SUCCESS")
                )
                ));


    }



/*    @Test
    void test1() throws Exception {
        mockMvc.perform(get("/")).andDo(print());
    }*/
}
