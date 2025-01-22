package com.example.word.common.domain.statistics.model;

import com.example.word.common.domain.statistics.model.enums.StatisticsStatus;
import com.example.word.common.domain.user.model.UserEntity;
import com.example.word.common.domain.word.model.WordEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatisticsResponse {

    private String userId;

    private Long wordId;

    private StatisticsStatus status;

    private int correctAnswerCount;

    private int noQuizCount;

    private Long totalQuizCount;

    private String word;

}
