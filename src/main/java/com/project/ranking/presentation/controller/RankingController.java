package com.project.ranking.presentation.controller;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.response.CommonResponse;
import com.project.ranking.application.dto.RankingListResponse;
import com.project.ranking.application.dto.RankingMeResponse;
import com.project.ranking.application.service.RankingQueryService;
import com.project.ranking.config.RankingProperties;
import com.project.ranking.domain.RankingPeriodType;
import com.project.ranking.presentation.swagger.RankingControllerDocs;
import com.project.user.application.dto.UserSession;

import io.swagger.v3.oas.annotations.Parameter;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingController implements RankingControllerDocs {

    private final RankingQueryService rankingQueryService;
    private final RankingProperties rankingProperties;

    @Override
    @GetMapping
    public ResponseEntity<CommonResponse<RankingListResponse>> list(
            @RequestParam("period_type") RankingPeriodType periodType,
            @RequestParam(value = "period_key", required = false) String periodKey,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", required = false) Integer size,
            @Parameter(hidden = true) HttpSession session) {
        int pageSize = size != null ? size : rankingProperties.getDefaultPageSize();
        Long me = extractUserId(session);
        RankingListResponse body = rankingQueryService.list(periodType, periodKey, page, pageSize, me);
        return ResponseEntity.ok(CommonResponse.ok(body));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<RankingMeResponse>> myRank(
            @RequestParam("period_type") RankingPeriodType periodType,
            @RequestParam(value = "period_key", required = false) String periodKey,
            @Parameter(hidden = true) HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("LOGIN_USER");
        if (user == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }
        RankingMeResponse body = rankingQueryService.myRank(periodType, periodKey, user.getUserId());
        return ResponseEntity.ok(CommonResponse.ok(body));
    }

    private static Long extractUserId(HttpSession session) {
        UserSession user = (UserSession) session.getAttribute("LOGIN_USER");
        return user != null ? user.getUserId() : null;
    }
}
