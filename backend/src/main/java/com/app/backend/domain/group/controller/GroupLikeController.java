package com.app.backend.domain.group.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.app.backend.domain.group.service.GroupLikeService;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.global.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/like")
@RequiredArgsConstructor
public class GroupLikeController {

    private final GroupLikeService groupLikeService;

    /** 현재 사용자가 해당 그룹을 좋아요했는지 여부 확인 */
    @GetMapping
    public ApiResponse<Boolean> isLiked(
            @PathVariable Long groupId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        boolean liked = groupLikeService.isLiked(groupId, memberDetails.getId());
        return ApiResponse.of(
                true,
                HttpStatus.OK,
                "좋아요 여부 확인 성공");
    }

    /** 그룹 좋아요 추가 */
    @PostMapping
    public ApiResponse<Void> likeGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        groupLikeService.likeGroup(groupId, memberDetails.getId());
        return ApiResponse.of(true, HttpStatus.OK, "좋아요 성공");
    }

    /** 그룹 좋아요 취소 */
    @DeleteMapping
    public ApiResponse<Void> unlikeGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        groupLikeService.unlikeGroup(groupId, memberDetails.getId());
        return ApiResponse.of(true, HttpStatus.OK, "좋아요 취소 성공");
    }
}
