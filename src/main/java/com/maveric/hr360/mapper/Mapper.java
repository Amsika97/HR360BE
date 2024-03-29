package com.maveric.hr360.mapper;


import com.maveric.hr360.dto.QuestionDto;
import com.maveric.hr360.dto.QuestionSectionDto;
import com.maveric.hr360.entity.QuestionSection;
import com.maveric.hr360.entity.Questions;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@org.mapstruct.Mapper
public interface Mapper {
    Mapper INSTANCE = Mappers.getMapper(Mapper.class);

    QuestionSection dtoToQuestionSection(QuestionSectionDto questionSectionDto);

    QuestionSectionDto entityToQuestionSectionDto(QuestionSection questionSection);
    @Mapping(target = "questionSection.id", source = "questionSectionId")
    Questions dtoToQuestion(QuestionDto questionDto);
    @Mapping(target = "questionSectionId", source = "questionSection.id")
    QuestionDto entityToQuestionDto(Questions question);

    List<Questions> dtoToQuestions(List<QuestionDto> questionDto);
    List<QuestionDto> entityToQuestionDtos(List<Questions> question);

}
