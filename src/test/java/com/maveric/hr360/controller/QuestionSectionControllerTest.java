package com.maveric.hr360.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maveric.hr360.dto.QuestionDto;
import com.maveric.hr360.dto.QuestionSectionDto;
import com.maveric.hr360.entity.QuestionSection;
import com.maveric.hr360.mapper.Mapper;
import com.maveric.hr360.service.QuestionSectionService;
import com.maveric.hr360.service.implementation.QuestionSectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionSectionController.class)
@ExtendWith(SpringExtension.class)
public class QuestionSectionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired


    private QuestionSectionController questionSectionController;
    @MockBean
    private QuestionSectionServiceImpl questionSectionService;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private Mapper mapper;
    private File jsonFile;
    private QuestionSectionDto questionSectionDto;
    @BeforeEach
    void intial() throws IOException {

        jsonFile = new ClassPathResource("questionsection.json").getFile();
        questionSectionDto = objectMapper.readValue(Files.readString(jsonFile.toPath()), QuestionSectionDto.class);

    }

    @Test
    void saveQuestionSection() throws Exception {
        QuestionSection questionSection =new QuestionSection();
        BeanUtils.copyProperties(questionSectionDto,questionSection);
        when(questionSectionService.saveQuestionSection(questionSectionDto)).thenReturn(questionSection);
        ResponseEntity<QuestionSectionDto> response = questionSectionController.saveQuestionSection(questionSectionDto);
        mockMvc.perform(post("/api/questionsection/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionSectionDto)))
                .andExpect(status().isOk());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(questionSectionDto, response.getBody());
    }

}
