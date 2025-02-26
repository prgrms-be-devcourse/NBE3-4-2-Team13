package com.app.backend.domain.group.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.backend.domain.group.service.GroupLikeService;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.global.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/like")
@RequiredArgsConstructor
public class GroupLikeController {

    private final GroupLikeService groupLikeService;

    @PostMapping
    public ApiResponse<Void> toggleLikeGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        boolean isLiked = groupLikeService.toggleLikeGroup(groupId, memberDetails.getId());

        return ApiResponse.of(
                true,
                HttpStatus.OK,
                isLiked ? "좋아요 성공" : "좋아요 취소 성공"
        );
    }
}