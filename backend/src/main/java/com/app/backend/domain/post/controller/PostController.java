package com.app.backend.domain.post.controller;

import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.post.dto.req.PostReqDto;
import com.app.backend.domain.post.dto.resp.PostRespDto;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.service.post.PostService;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.error.exception.GlobalErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/post")
public class PostController {

    private final PostService postService;

    @GetMapping("/{id}")
    public ApiResponse<?> getPost(
            @PathVariable("id") final Long postId,
            @AuthenticationPrincipal final MemberDetails memberDetails
    ) {

        PostRespDto.GetPostDto post = postService.getPost(postId, memberDetails.getId());

        return ApiResponse.of(true, HttpStatus.OK, "게시글을 성공적으로 불러왔습니다", post);
    }

    @GetMapping
    public ApiResponse<?> getPosts(
            @RequestParam final Long groupId,
            @RequestParam(defaultValue = "") final String search,
            @RequestParam(defaultValue = "ALL") final PostStatus postStatus,
            @PageableDefault Pageable pageable
    ) {

        Page<PostRespDto.GetPostListDto> posts = postService.getPostsBySearch(groupId, search, postStatus, pageable);

        return ApiResponse.of(true, HttpStatus.OK, "게시물 목록을 성공적으로 불러왔습니다", posts);
    }

    @PostMapping
    public ApiResponse<?> savePost(
            @Valid @RequestPart("post") final PostReqDto.SavePostDto savePost,
            @RequestPart(value = "file", required = false) final MultipartFile[] files,
            final BindingResult bindingResult,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {

        if (bindingResult.hasErrors()) {
            throw new PostException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }

        Post post = postService.savePost(memberDetails.getId(), savePost, files);

        return ApiResponse.of(true, HttpStatus.OK, "게시글이 성공적으로 저장되었습니다", new PostRespDto.GetPostIdDto(post.getId()));
    }

    @PatchMapping("/{id}")
    public ApiResponse<?> updatePost(
            @PathVariable("id") final Long id,
            @Valid @RequestPart("post") final PostReqDto.ModifyPostDto modifyPost,
            @RequestPart(value = "file", required = false) final MultipartFile[] files,
            final BindingResult bindingResult,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {

        if (bindingResult.hasErrors()) {
            throw new PostException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }

        Post post = postService.updatePost(memberDetails.getId(), id, modifyPost, files);

        return ApiResponse.of(true, HttpStatus.OK, "게시글이 성공적으로 수정되었습니다", new PostRespDto.GetPostIdDto(post.getId()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deletePost(
            @PathVariable("id") final Long id,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {

        postService.deletePost(memberDetails.getId(), id);

        return ApiResponse.of(true, HttpStatus.OK, "게시글이 성공적으로 삭제되었습니다");
    }

    @GetMapping("/members")
    public ApiResponse<?> getMembers(
            @Valid @ModelAttribute final PostReqDto.SearchPostDto searchPost,
            @PageableDefault Pageable pageable,
            final BindingResult bindingResult,
            @AuthenticationPrincipal final MemberDetails memberDetails) {

        if (bindingResult.hasErrors()) {
            throw new PostException(GlobalErrorCode.INVALID_INPUT_VALUE);
        }

        Page<PostRespDto.GetPostListDto> posts = postService.getPostsByUser(searchPost, pageable, memberDetails.getId());

        return ApiResponse.of(true, HttpStatus.OK, "게시물 목록을 성공적으로 불러왔습니다", posts);
    }
}
