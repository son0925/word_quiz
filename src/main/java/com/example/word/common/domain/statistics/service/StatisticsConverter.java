package com.example.word.common.domain.statistics.service;

import com.example.word.common.annotation.Converter;
import com.example.word.common.domain.statistics.model.StatisticsEntity;
import com.example.word.common.domain.statistics.model.StatisticsResponse;
import com.example.word.common.domain.user.service.UserConverter;
import com.example.word.common.domain.word.service.WordConverter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class StatisticsConverter {

    private final UserConverter userConverter;
    private final WordConverter wordConverter;

    public StatisticsResponse toResponse(StatisticsEntity entity) {
        return StatisticsResponse.builder()
                .userId(entity.getId().getUserId())
                .wordId(entity.getId().getWordId())
                .status(entity.getStatus())
                .correctAnswerCount(entity.getCorrectAnswerCount())
                .totalQuizCount(entity.getTotalQuizCount())
                .noQuizCount(entity.getNoQuizCount())
                .word(entity.getWord().getWord())
                .build();
    }
}
