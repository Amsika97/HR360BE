//package com.maveric.hr360.service;
//
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.maveric.hr360.dto.QuestionDto;
//import com.maveric.hr360.dto.QuestionSectionDto;
//import com.maveric.hr360.entity.QuestionSection;
//import com.maveric.hr360.entity.Questions;
//import com.maveric.hr360.mapper.Mapper;
//import com.maveric.hr360.repository.QuestionSectionRepository;
//import com.maveric.hr360.repository.QuestionsRepository;
//import com.maveric.hr360.service.implementation.QuestionSectionServiceImpl;
//import com.maveric.hr360.service.implementation.QuestionServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//@ExtendWith({SpringExtension.class})
//@SpringBootTest
//public class QuestionServiceTest {
//    @MockBean
//    private QuestionsRepository questionsRepository;
//    @Autowired
//    private QuestionServiceImpl questionService;
//    @Autowired
//    private ObjectMapper objectMapper;
//    private File jsonFile;
//    private List<QuestionDto> questionDtoList;
//
//    @BeforeEach
//    void initialize() throws IOException {
//        jsonFile = new ClassPathResource("questions.json").getFile();
//        questionDtoList = objectMapper.readValue(Files.readString(jsonFile.toPath()), new TypeReference<List<QuestionDto>>() {});
//    }
//
//    @Test
//    void saveQuestions() {
//List<Questions> questionsList = Mapper.INSTANCE.dtoToQuestions(questionDtoList);
//
//        List<Questions> question = new ArrayList<>();
//        BeanUtils.copyProperties(questionDtoList, question);
//        when(questionsRepository.saveAll(question)).thenReturn(question);
//        List<Questions> result=questionService.saveQuestion(questionDtoList);
//        assertEquals(question,result);
//    }
//
//    @Test
//    void saveQuestionsThrowsIllegalArgumentException() {
//        List<QuestionDto> questionDtoList = null;
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//            questionService.saveQuestion(questionDtoList);
//        });
//        assertEquals("Request isnot valid", exception.getMessage());
//    }
//}
