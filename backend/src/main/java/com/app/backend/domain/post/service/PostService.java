package com.app.backend.domain.post.service;

import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import com.app.backend.domain.attachment.service.FileService;
import com.app.backend.domain.attachment.util.FileUtil;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.exception.GroupErrorCode;
import com.app.backend.domain.group.exception.GroupException;
import com.app.backend.domain.group.repository.GroupRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final FileService fileService;
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final PostAttachmentRepository postAttachmentRepository;

    public PostRespDto.GetPostDto getPost(final Long postId, final Long memberId) {

        // Todo : 권한 체크 ex) private, notice

        Post post = postRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        if (post.getDisabled()) {
            throw new PostException(PostErrorCode.POST_NOT_FOUND);
        }

        List<PostAttachmentRespDto.GetPostAttachment> attachments = postAttachmentRepository
                .findByPostId(postId).stream().map(PostAttachmentRespDto.GetPostAttachment::new).toList();

        return new PostRespDto.GetPostDto(post, attachments);
    }

    // Todo : paging 처리, 검색 기능
    public void getPosts(final Long memberId, final String fileType, final int page) {
        List<Sort.Order> sort = new ArrayList<>();
        sort.add(new Sort.Order(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, 5, Sort.by(sort));

    }

    @Transactional
    public Post savePost(final Long memberId, final PostReqDto.SavePost savePost, final MultipartFile[] files) {

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new PostException(GlobalErrorCode.ENTITY_NOT_FOUND));

        Group group = groupRepository.findById(savePost.getGroupId()).orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

        // Todo: Group 의 일원인지 확인, 상태가 APPROVED 인지 확인

        // Todo: 게시글 종류가 NOTICE 라면 LEADER 인지 확인

        Post post = postRepository.save(savePost.toEntity(group, member));

        // Todo : 코드 리펙토링 개선
        if (files != null) {
            List<PostAttachment> attachments = saveFiles(files, post.getId());
            try {
                postAttachmentRepository.saveAll(attachments);
            } catch (Exception e) {
                for (PostAttachment attachment : attachments) {
                    fileService.deleteFile(attachment.getStoreFilePath());
                }
            }
        }

        return post;
    }

    @Transactional
    public Post updatePost(final Long memberId, final Long postId, final PostReqDto.ModifyPost modifyPost, final MultipartFile[] files) {

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new PostException(GlobalErrorCode.ENTITY_NOT_FOUND));

        Post post = postRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        // Todo : 권한 체크 -> Group Leader 인지 확인
        if (!post.getMember().getId().equals(memberId)) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        if (modifyPost.getOldFileSize() + (files != null ? Arrays.stream(files).mapToLong(MultipartFile::getSize).sum() : 0) > 10 * 1024 * 1024) {
            throw new FileException(FileErrorCode.FILE_SIZE_EXCEEDED);
        }


        if (files != null) {
            List<PostAttachment> attachments = saveFiles(files, post.getId());
            try {
                postAttachmentRepository.saveAll(attachments);
                if (modifyPost.getRemoveIdList() != null && !modifyPost.getRemoveIdList().isEmpty()) {
                    postAttachmentRepository.deleteByIdList(modifyPost.getRemoveIdList());
                }
            } catch (Exception e) {
                // Todo : 비동기 처리 필요
                for (PostAttachment attachment : attachments) {
                    fileService.deleteFile(attachment.getStoreFilePath());
                }
                throw e;
            }
        }

        // 게시물 수정

        post.setTitle(modifyPost.getTitle());
        post.setContent(modifyPost.getContent());
        post.setPostStatus(modifyPost.getPostStatus());

        return post;
    }

    @Transactional
    public void deletePost(final Long memberId, final Long postId) {

        Post post = postRepository.findById(postId).orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));

        // Todo : 권한 체크 -> Group LEADER 인지 확인

        if (!post.getMember().getId().equals(memberId)) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        post.delete();
    }

    private List<PostAttachment> saveFiles(final MultipartFile[] files, final Long postId) {
        List<PostAttachment> attachments = new ArrayList<>();
        List<String> saveFilePaths = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String filePath = fileService.saveFile(file);
                String fileName = FileUtil.getFileName(filePath);
                saveFilePaths.add(filePath);

                attachments.add(new PostAttachment(
                        file.getOriginalFilename(),
                        fileName,
                        filePath,
                        file.getSize(),
                        FileUtil.getFileType(fileName),
                        postId));
            }
        } catch (Exception e) {
            // Todo : 오류 검출시 롤백 -> 비동기 처리 필요
            if (!saveFilePaths.isEmpty()) {
                for (String filePath : saveFilePaths) {
                    fileService.deleteFile(filePath);
                }
            }
            throw e;
        }

        return attachments;
    }
}
