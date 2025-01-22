package com.example.word.common.domain.statistics.model;

import com.example.word.common.domain.statistics.model.enums.StatisticsStatus;
import com.example.word.common.domain.user.model.UserEntity;
import com.example.word.common.domain.word.model.WordEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "statistics")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsEntity {

    @EmbeddedId
    private StatisticsId id;

    @Enumerated(EnumType.STRING)
    private StatisticsStatus status;

    private int correctAnswerCount;

    private int noQuizCount;

    private long totalQuizCount;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private UserEntity user;

    @ManyToOne
    @MapsId("wordId")
    @JoinColumn(name = "word_id", nullable = false)
    @JsonBackReference
    private WordEntity word;

}

