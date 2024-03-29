package com.maveric.hr360.service;

import com.maveric.hr360.dto.QuestionDto;
import com.maveric.hr360.entity.Questions;

import java.util.List;

public interface QuestionService {
    List<Questions> saveQuestion(List<QuestionDto> questionDto);
}
