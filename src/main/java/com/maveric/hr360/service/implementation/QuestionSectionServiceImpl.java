package com.maveric.hr360.service.implementation;


import com.maveric.hr360.dto.QuestionSectionDto;
import com.maveric.hr360.entity.QuestionSection;
import com.maveric.hr360.mapper.Mapper;
import com.maveric.hr360.repository.QuestionSectionRepository;
import com.maveric.hr360.service.QuestionSectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionSectionServiceImpl implements QuestionSectionService {
    private final QuestionSectionRepository questionSectionRepository;
    @Override
    public QuestionSection saveQuestionSection(QuestionSectionDto questionSectionDto) {
        log.info("QuestionSectionServiceImpl::saveQuestionSection()::Start");
        if (Objects.isNull(questionSectionDto)) {
            throw new IllegalArgumentException("Request isnot valid");
        }
        QuestionSection questionSection= Mapper.INSTANCE.dtoToQuestionSection(questionSectionDto);
        log.info("QuestionSectionServiceImpl::saveQuestionSection()::end");
        return questionSectionRepository.save(questionSection);
    }
}
