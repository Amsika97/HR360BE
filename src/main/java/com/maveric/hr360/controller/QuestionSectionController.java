package com.maveric.hr360.controller;

import com.maveric.hr360.dto.QuestionSectionDto;
import com.maveric.hr360.mapper.Mapper;
import com.maveric.hr360.service.QuestionSectionService;
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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/questionsection")
public class QuestionSectionController {
    private final QuestionSectionService questionSectionService;

    @Operation(
            summary = "Save QuestionSection"
    )
    @ApiResponses(value = {@ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = QuestionSectionDto.class))
    )})
    @PostMapping(path = "/save")
    public ResponseEntity<QuestionSectionDto> saveQuestionSection(@RequestBody @Valid QuestionSectionDto request) {
        log.debug("QuestionSectionController::saveQuestionSection()::{}", request);
        return ResponseEntity
                .ok(Mapper.INSTANCE.entityToQuestionSectionDto(questionSectionService.saveQuestionSection(request)));
    }

}
