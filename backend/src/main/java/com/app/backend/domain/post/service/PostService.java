package com.app.backend.domain.post.service;

import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import com.app.backend.domain.attachment.service.FileService;
import com.app.backend.domain.attachment.util.FileUtil;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.dto.req.PostReqDto;
import com.app.backend.domain.post.dto.resp.PostAttachmentRespDto;
import com.app.backend.domain.post.dto.resp.PostRespDto;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.domain.post.exception.PostErrorCode;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository;
import com.app.backend.global.error.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final FileService fileService;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final PostAttachmentRepository postAttachmentRepository;

    public PostRespDto.GetPostDto getPost(final Long postId, final Long memberId) {
        // Todo : 권한 체크 ex) private, notice

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new PostException(GlobalErrorCode.ENTITY_NOT_FOUND));

        Post post = postRepository.findByIdAndDisabled(postId, false).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        if (post.getDisabled()) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }

        List<PostAttachmentRespDto.GetPostAttachmentDto> attachments = postAttachmentRepository
                .findByPostId(postId).stream().map(PostAttachmentRespDto.GetPostAttachmentDto::new).toList();

        return new PostRespDto.GetPostDto(post, member, attachments);
    }

    public Page<PostRespDto.GetPostListDto> getPostsBySearch(final PostReqDto.SearchPostDto searchPost, final Pageable pageable) {
        return postRepository.findAllBySearchStatus(searchPost.getGroupId(), searchPost.getSearch(), searchPost.getPostStatus(), false, pageable)
                .map(PostRespDto.GetPostListDto::new);
    }

    @Transactional
    public Post savePost(final Long memberId, final PostReqDto.SavePostDto savePost, final MultipartFile[] files) {
        // Todo: Group 의 일원인지 확인, 상태가 APPROVED 인지 확인
        // Todo: 게시글 종류가 NOTICE 라면 LEADER 인지 확인

        Post post = postRepository.save(savePost.toEntity(memberId));

        saveFiles(files, post.getId());

        return post;
    }

    @Transactional
    public Post updatePost(final Long memberId, final Long postId, final PostReqDto.ModifyPostDto modifyPost, final MultipartFile[] files) {
        Post post = postRepository.findByIdAndDisabled(postId, false).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        // Todo : 권한 체크 -> Group Leader 인지 확인
        if (!post.getMemberId().equals(memberId)) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        checkFileSize(files, modifyPost.getOldFileSize(), (long) (10 * 1024 * 1024));

        post.setTitle(modifyPost.getTitle());
        post.setContent(modifyPost.getContent());
        post.setPostStatus(modifyPost.getPostStatus());

        saveFiles(files, post.getId());

        if (modifyPost.getRemoveIdList() != null && !modifyPost.getRemoveIdList().isEmpty()) {
            postAttachmentRepository.deleteByIdList(modifyPost.getRemoveIdList());
        }

        return post;
    }

    @Transactional
    public void deletePost(final Long memberId, final Long postId) {
        Post post = postRepository.findByIdAndDisabled(postId, false).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        // Todo : 권한 체크 -> Group LEADER 인지 확인

        if (!post.getMemberId().equals(memberId)) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        post.delete();
    }

    // 파일 크기 체크
    private void checkFileSize(final MultipartFile[] files, final Long oldSize, final Long maxSize) {
        Long newSize = files != null ? Arrays.stream(files).mapToLong(MultipartFile::getSize).sum() : 0;
        if (oldSize + newSize > maxSize) {
            throw new FileException(FileErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    // 파일 저장 및 롤백
    private void saveFiles(final MultipartFile[] files, final Long postId) {
        if (files == null || files.length == 0) return;

        List<PostAttachment> attachments = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String filePath = fileService.saveFile(file);
                String fileName = FileUtil.getFileName(filePath);
                filePaths.add(filePath);

                attachments.add(new PostAttachment(
                        file.getOriginalFilename(),
                        fileName,
                        filePath,
                        file.getSize(),
                        FileUtil.getFileType(fileName),
                        postId));
            }
            postAttachmentRepository.saveAll(attachments);
        } catch (Exception e) {
            if (!attachments.isEmpty()) {
                fileService.deleteFiles(filePaths);
            }
            throw e;
        }
    }
}