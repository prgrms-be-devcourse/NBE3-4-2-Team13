package com.app.backend.domain.post.controller;

import com.app.backend.domain.post.dto.req.PostReqDto;
import com.app.backend.domain.post.dto.resp.PostRespDto;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.service.PostService;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.error.exception.GlobalErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ApiResponse<?> savePost(
            @Valid @RequestPart("post") final PostReqDto.SavePost savePost,
            @RequestPart(value = "file", required = false) final MultipartFile[] files,
            final BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (bindingResult.hasErrors()) {
            throw new PostException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }

        // Todo: userDetails 에 memberId 값 필요 (jwt 토큰값에 id 필드 생성 필요). save param 으로 같이 넘김
        Long memberId = 1l;

        Post post = postService.savePost(memberId, savePost, files);

        return ApiResponse.of(
                true,
                String.valueOf(HttpStatus.OK.value()),
                "게시글이 성공적으로 저장되었습니다",
                new PostRespDto.PostIdDto(post.getId()));
    }

    @PatchMapping("/{id}")
    public ApiResponse<?> updatePost(
            @PathVariable("id") final Long id,
            @Valid @RequestPart("post") final PostReqDto.ModifyPost modifyPost,
            @RequestPart(value = "file", required = false) final MultipartFile[] files,
            final BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        if (bindingResult.hasErrors()) {
            throw new PostException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }

        // Todo : userDetails 에 memberId 값 필요 (jwt 토큰값에 id 필드 생성 필요). update param 으로 같이 넘김
        Long memberId = 1l;
        Post post = postService.updatePost(memberId,id,modifyPost,files);

        return ApiResponse.of(
                true,
                String.valueOf(HttpStatus.OK.value()),
                "수정을 완료했습니다",
                new PostRespDto.PostIdDto(post.getId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deletePost(
            @PathVariable("id") final Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        // Todo : userDetails 에 memberId 값 필요 (jwt 토큰값에 id 필드 생성 필요). delete param 으로 같이 넘김
        Long memberId = 1l;
        postService.deletePost(memberId, id);

        return ApiResponse.of(
                true,
                String.valueOf(HttpStatus.OK.value()),
                "삭제를 완료했습니다."
        );
    }
}
