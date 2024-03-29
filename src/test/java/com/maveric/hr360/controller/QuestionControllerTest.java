package com.maveric.hr360.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maveric.hr360.dto.QuestionDto;
import com.maveric.hr360.entity.Questions;
import com.maveric.hr360.mapper.Mapper;
import com.maveric.hr360.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionController.class)
@ExtendWith(SpringExtension.class)
public class QuestionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private QuestionController questionController;
    @MockBean
    private QuestionService questionService;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private Mapper mapper;

    @Test
    void saveQuestionTest() throws IOException, Exception {
        List<QuestionDto> questionDto=new ArrayList<>();
        when(mapper.entityToQuestionDtos(questionService.saveQuestion(questionDto)))
                .thenReturn(questionDto);
        ResponseEntity<List<QuestionDto>> response = questionController.saveQuestion(questionDto);
        mockMvc.perform(post("/api/question/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionDto)))
                .andExpect(status().isOk());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(questionDto, response.getBody());
    }

}
