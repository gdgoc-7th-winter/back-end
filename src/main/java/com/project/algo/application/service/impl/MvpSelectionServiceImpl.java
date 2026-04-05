package com.project.algo.application.service.impl;

import com.project.algo.application.service.MvpSelectionService;
import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.entity.DailyMVP;
import com.project.algo.domain.repository.AnswerCodePostRepository;
import com.project.algo.domain.repository.DailyMVPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MvpSelectionServiceImpl implements MvpSelectionService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final int MVP_COUNT = 3;
    private static final PageRequest MVP_PAGE = PageRequest.of(
            0, MVP_COUNT,
            Sort.by(Sort.Direction.DESC, "likeCount").and(Sort.by(Sort.Direction.ASC, "createdAt"))
    );

    private final AnswerCodePostRepository answerCodePostRepository;
    private final DailyMVPRepository dailyMVPRepository;

    @Override
    @Transactional
    public void selectDailyMvps(LocalDate targetDate) {
        // createdAt(UTC Instant) 기준으로 KST 해당 날짜의 시작~종료 범위 계산
        Instant startOfDay = targetDate.atStartOfDay(SEOUL).toInstant();
        Instant endOfDay   = targetDate.plusDays(1).atStartOfDay(SEOUL).toInstant();

        // challenge 구분 없이 당일 제출된 전체 풀이 중 likeCount 상위 3개 선정
        List<AnswerCodePost> top3 = answerCodePostRepository
                .findTop3ForMvp(startOfDay, endOfDay, MVP_PAGE);

        if (top3.isEmpty()) {
            log.info("[MvpSelection] {} 에 제출된 풀이 없음 — MVP 선정 건너뜀", targetDate);
            return;
        }

        // 멱등성: 날짜 단위로 기존 MVP 전체 삭제 후 재저장 (수동 재실행 시에도 안전)
        dailyMVPRepository.deleteByAwardedAt(targetDate);

        for (int i = 0; i < top3.size(); i++) {
            AnswerCodePost answer = top3.get(i);
            DailyMVP mvp = DailyMVP.of(
                    answer.getDailyChallenge(),
                    answer.getAuthor(),
                    i + 1,                  // rank: 1, 2, 3
                    answer.getLikeCount(),
                    targetDate
            );
            dailyMVPRepository.save(mvp);

            log.debug("[MvpSelection] rank={} userId={} likeCount={} challengeId={}",
                    i + 1, answer.getAuthor().getId(), answer.getLikeCount(),
                    answer.getDailyChallenge().getId());
        }

        log.info("[MvpSelection] {} MVP 선정 완료", targetDate);
    }
}
