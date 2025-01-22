package com.example.word.common.domain.statistics.business;

import com.example.word.common.annotation.Business;
import com.example.word.common.domain.statistics.model.*;
import com.example.word.common.domain.statistics.service.StatisticsConverter;
import com.example.word.common.domain.statistics.service.StatisticsService;
import com.example.word.common.domain.user.business.UserBusiness;
import com.example.word.common.domain.user.model.User;
import com.example.word.common.domain.user.model.UserEntity;
import com.example.word.common.domain.word.model.WordEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@Business
@RequiredArgsConstructor
public class StatisticsBusiness {

    private final StatisticsService statisticsService;

    private final StatisticsConverter statisticsConverter;

    private final UserBusiness userBusiness;


    public void create(WordEntity word, UserEntity user) {
        statisticsService.create(word, user);
    }

    public List<StatisticsResponse> read(User user) {
        var userId = user.getUserId();

        var entityList = statisticsService.getStatisticsList(userId);

        return entityList.stream()
                .map(statisticsConverter::toResponse)
                .toList();
    }

    public void updateStatistics(WordEntity wordEntity) {

        var userEntity = wordEntity.getUser();

        statisticsService.updateStatistics(wordEntity, userEntity);

    }

    public void delete(StatisticsId id) {
        statisticsService.deleteWord(id);
    }


    public List<StatisticsResponse> getWordQuizList(User user, int size) {
        var userId = userBusiness.findByIdWithThrow(user).getUserId();

        var statisticsResponseList =  statisticsService.getWordQuizList(userId, size).stream()
                .map(statisticsConverter::toResponse)
                .toList();

        log.info(statisticsResponseList.toString());

        return statisticsResponseList;
    }

    public void resultUpdate(User user, List<StatisticsUpdateRequest> req) {
        var userId = userBusiness.findByIdWithThrow(user).getUserId();

        statisticsService.resultUpdate(userId, req);
    }

    public StatisticsEntity getStatisticsWithThrow(WordEntity wordEntity, UserEntity userEntity) {

        return statisticsService.getStatisticsEntityWithThrow(wordEntity, userEntity);
    }

    public List<PivotResponse> getPivot(User user) {

        var userId = userBusiness.findByUserWithThrow(user).getUserId();

        return statisticsService.getPivotList(userId);
    }
}
