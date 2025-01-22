package com.example.word.common.domain.statistics.service;

import com.example.word.common.domain.statistics.db.StatisticsRepository;
import com.example.word.common.domain.statistics.model.PivotResponse;
import com.example.word.common.domain.statistics.model.StatisticsEntity;
import com.example.word.common.domain.statistics.model.StatisticsId;
import com.example.word.common.domain.statistics.model.StatisticsUpdateRequest;
import com.example.word.common.domain.statistics.model.enums.StatisticsStatus;
import com.example.word.common.domain.streaks.business.StreaksBusiness;
import com.example.word.common.domain.user.model.UserEntity;
import com.example.word.common.domain.word.model.WordEntity;
import com.example.word.common.error.ErrorCode;
import com.example.word.common.error.StatisticsErrorCode;
import com.example.word.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;

    private final StreaksBusiness streaksBusiness;

    public void create(WordEntity word, UserEntity user) {

        var id = StatisticsId.builder()
                .userId(user.getUserId())
                .wordId(word.getWordId())
                .build();

        if (statisticsRepository.existsById(id)) {
            throw new ApiException(StatisticsErrorCode.SAVE_FAILED);
        }

        var statisticsEntity = StatisticsEntity.builder()
                .id(StatisticsId.builder()
                        .userId(id.getUserId())
                        .wordId(id.getWordId())
                        .build())
                .status(StatisticsStatus.NO_ANSWER)
                .correctAnswerCount(0)
                .totalQuizCount(0L)
                .noQuizCount(0)
                .word(word)
                .user(user)
                .build();


        statisticsRepository.save(statisticsEntity);
    }


    // 특정 사용자가 현재 자신의 통계를 요청
    public List<StatisticsEntity> getStatisticsList(String userId) {
        return statisticsRepository.findAllByIdUserId(userId);
    }


    public void deleteWord(StatisticsId id) {
        statisticsRepository.deleteById(id);
    }

    // 단어 퀴즈 알고리즘

    /**
     * 1. 퀴즈를 한 번도 하지 않은 단어
     * 2. 마지막 퀴즈에서 틀린 단어
     * 3. 퀴즈를 5번 이하로 한 단어(아직 익숙하지 않기 때문에 추가)
     * 4. Math.max(statisticsList.size() / 10, 5) 번 문제를 풀지 않은 단어
     * 5. 정답률이 저조한 단어
     */
    public List<StatisticsEntity> getWordQuizList(String userId, int size) {
        var statisticsList = statisticsRepository.findAllByIdUserId(userId);


        // 퀴즈를 한 번도 하지 않은 단어
        var wordQuizList = statisticsList.stream()
                .filter(it -> it.getStatus().equals(StatisticsStatus.NO_ANSWER))
                .collect(Collectors.toList());


        // size 채웠다면 바로 return 하기
        if (wordQuizList.size() >= size) {
            return wordQuizList.subList(0, size);
        }

        // 마지막 퀴즈에서 틀린 단어를 추가
        statisticsList.stream()
                .filter(it -> it.getStatus().equals(StatisticsStatus.WRONG_ANSWER))
                .filter(it -> !wordQuizList.contains(it))  // 이미 포함된 단어는 제외
                .forEach(wordQuizList::add);

        // size 채웠다면 바로 return 하기
        if (wordQuizList.size() >= size) {
            return wordQuizList.subList(0, size);
        }

        // 퀴즈를 5번 이하로 했는 단어
        statisticsList.stream()
                .filter(it -> it.getTotalQuizCount() <= 3)
                .filter(it -> !wordQuizList.contains(it))
                .forEach(wordQuizList::add);

        if (wordQuizList.size() >= size) {
            return wordQuizList.subList(0, size);
        }

        int count = Math.max(statisticsList.size() / 10, 5);

        // 5번 이상 퀴즈를 하지 않은 단어
        statisticsList.stream()
                .filter(it -> it.getNoQuizCount() >= count)
                .filter(it -> !wordQuizList.contains(it))  // 이미 포함된 단어는 제외
                .forEach(wordQuizList::add);

        // size 채웠다면 바로 return 하기
        if (wordQuizList.size() >= size) {
            return wordQuizList.subList(0, size);
        }

        // 퀴즈를 풀었지만 틀린 단어들 (정답률에 따라 정렬)
        var sortList = new ArrayList<>(statisticsList.stream()
                .filter(it -> it.getNoQuizCount() <= 10)
                .filter(it -> !wordQuizList.contains(it))
                .toList());

        // 정답률에 따른 오름차순 정렬
        sortList.sort((o1, o2) -> {
            double o1CorrectRate = o1.getCorrectAnswerCount() / (double) o1.getTotalQuizCount();
            double o2CorrectRate = o2.getCorrectAnswerCount() / (double) o2.getTotalQuizCount();
            return Double.compare(o1CorrectRate, o2CorrectRate);
        });

        // 부족한 개수만큼 퀴즈를 풀었지만 틀린 단어들에서 추가
        int remainingSize = size - wordQuizList.size();
        wordQuizList.addAll(sortList.subList(0, Math.min(remainingSize, sortList.size())));


        return wordQuizList;
    }


    // 통계 결과 업데이트
    public void resultUpdate(String userId, List<StatisticsUpdateRequest> statisticsList) {
        // 퀴즈 했던 단어 상태 변경하기
        for (StatisticsUpdateRequest result : statisticsList) {
            updateStatus(StatisticsId.builder()
                    .userId(userId)
                    .wordId(result.getWordId())
                    .build()
                    , result.getStatus());
        }

        // 퀴즈 했던 단어 id 가지고 오기
        var wordIdList = statisticsList.stream()
                .map(StatisticsUpdateRequest::getWordId)
                .toList();

        // 퀴즈를 하지 않은 단어 가지고 오기
        var noAnswerEntities = statisticsRepository.findAllByIdUserIdAndIdWordIdNotIn(userId, wordIdList);

        // 퀴즈를 하지 않은 단어 업데이트하기
        noAnswerEntities.forEach(it -> {
            it.setNoQuizCount(it.getNoQuizCount() + 1);
        });

        // 잔디 업데이트 하기
        streaksBusiness.plantingGrass(userId);

        // 저장하기
        statisticsRepository.saveAll(noAnswerEntities);
    }

    // 상태 변경하기
    public void updateStatus(StatisticsId id, StatisticsStatus status) {
        var entity = statisticsRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NULL_POINT));

        entity.setStatus(status);
        if (StatisticsStatus.ANSWER.equals(status)) {
            entity.setCorrectAnswerCount(entity.getCorrectAnswerCount() + 1);
        }

        entity.setTotalQuizCount(entity.getTotalQuizCount() + 1);
        entity.setNoQuizCount(0);

        statisticsRepository.save(entity);
    }

    public StatisticsEntity getStatisticsEntityWithThrow(WordEntity wordEntity, UserEntity userEntity) {
        return statisticsRepository.findByWordAndUser(wordEntity, userEntity)
                .orElseThrow(() -> new ApiException(ErrorCode.BAD_REQUEST));
    }

    // 통계 정보 변경하기
    public void updateStatistics(WordEntity wordEntity, UserEntity userEntity) {
        var optionalStatisticsEntity = statisticsRepository.findByWordAndUser(wordEntity, userEntity);

        if (optionalStatisticsEntity.isEmpty()) {
            throw new ApiException(ErrorCode.NULL_POINT);
        }

        var statisticsEntity = optionalStatisticsEntity.get();

        statisticsEntity.setStatus(StatisticsStatus.NO_ANSWER);
        statisticsEntity.setNoQuizCount(0);
        statisticsEntity.setCorrectAnswerCount(0);
        statisticsEntity.setTotalQuizCount(0);

        statisticsRepository.save(statisticsEntity);
    }

    public List<PivotResponse> getPivotList(String userId) {
        return statisticsRepository.findStatisticsByUserId(userId);
    }
}
