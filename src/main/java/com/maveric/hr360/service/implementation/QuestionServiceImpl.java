package com.maveric.hr360.service.implementation;

import com.maveric.hr360.dto.QuestionDto;
import com.maveric.hr360.dto.QuestionSectionDto;
import com.maveric.hr360.entity.QuestionSection;
import com.maveric.hr360.entity.Questions;
import com.maveric.hr360.mapper.Mapper;
import com.maveric.hr360.repository.QuestionSectionRepository;
import com.maveric.hr360.repository.QuestionsRepository;
import com.maveric.hr360.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    private final QuestionsRepository questionsRepository;
    @Override
    public List<Questions> saveQuestion(List<QuestionDto> questionDto) {
        log.info("QuestionServiceImpl::saveQuestion()::Start");
        if (Objects.isNull(questionDto)) {
            throw new IllegalArgumentException("Request isnot valid");
        }
        List<Questions> question= Mapper.INSTANCE.dtoToQuestions(questionDto);
        log.info("QuestionSectionImpl::saveQuestion()::end");
        return questionsRepository.saveAll(question);
    }
}
