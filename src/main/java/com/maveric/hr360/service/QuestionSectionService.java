package com.maveric.hr360.service;

import com.maveric.hr360.dto.QuestionSectionDto;
import com.maveric.hr360.entity.QuestionSection;

public interface QuestionSectionService {
    QuestionSection saveQuestionSection(QuestionSectionDto questionSectionDto);
}
