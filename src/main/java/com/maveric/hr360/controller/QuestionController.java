package com.maveric.hr360.controller;

import com.maveric.hr360.dto.QuestionDto;
import com.maveric.hr360.mapper.Mapper;
import com.maveric.hr360.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/question")
public class QuestionController {
    private final QuestionService questionService;

    @Operation(
            summary = "Save QuestionSection"
    )
    @ApiResponses(value = {@ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = QuestionDto.class))
    )})
    @PostMapping(path = "/save")
    public ResponseEntity<List<QuestionDto>> saveQuestion(@RequestBody @Valid List<QuestionDto> request) {
        log.debug("QuestionController::saveQuestion()::{}", request);
        return ResponseEntity
                .ok(Mapper.INSTANCE.entityToQuestionDtos(questionService.saveQuestion(request)));
    }

}
