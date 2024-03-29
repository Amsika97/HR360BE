//package com.maveric.hr360.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.maveric.hr360.dto.QuestionSectionDto;
//import com.maveric.hr360.entity.QuestionSection;
//import com.maveric.hr360.mapper.Mapper;
//import com.maveric.hr360.repository.QuestionSectionRepository;
//import com.maveric.hr360.service.implementation.QuestionSectionServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.http.HttpStatus;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.when;
//
//@ExtendWith({SpringExtension.class})
//@SpringBootTest
//public class QuestionSectionServiceTest {
//    @MockBean
//    private QuestionSectionRepository questionSectionRepository;
//    @Autowired
//    private QuestionSectionServiceImpl questionSectionService;
//    @Autowired
//    private ObjectMapper objectMapper;
//    private File jsonFile;
//    private QuestionSectionDto questionSectionDto;
//
//    @BeforeEach
//    void intial() throws IOException {
//        jsonFile = new ClassPathResource("questionsection.json").getFile();
//        questionSectionDto = objectMapper.readValue(Files.readString(jsonFile.toPath()), QuestionSectionDto.class);
//
//    }
//
//
//    @Test
//    void saveQuestionSection() {
//        QuestionSection questionSection = new QuestionSection();
//        BeanUtils.copyProperties(questionSectionDto, questionSection);
//        /*when(Mapper.INSTANCE.dtoToQuestionSection(questionSectionDto)).thenReturn(questionSection);*/
//
//        when(questionSectionRepository.save(questionSection)).thenReturn(questionSection);
//        QuestionSection result = questionSectionService.saveQuestionSection(questionSectionDto);
//        assertEquals(questionSection, result);
//    }
//
//    @Test
//    void saveQuestionSectionThrowsIllegalArgsException() {
//        QuestionSection questionSection = null;
//        QuestionSectionDto questionSectionDto1 = null;
//        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
//            questionSectionService.saveQuestionSection(questionSectionDto1);
//        });
//
//        assertEquals("Request isnot valid", illegalArgumentException.getMessage());
//    }
//}
