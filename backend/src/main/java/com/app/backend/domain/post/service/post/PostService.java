package com.app.backend.domain.post.service.post;

import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import com.app.backend.domain.attachment.service.FileService;
import com.app.backend.domain.attachment.util.FileUtil;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.exception.GroupMembershipErrorCode;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.dto.req.PostReqDto;
import com.app.backend.domain.post.dto.resp.PostAttachmentRespDto;
import com.app.backend.domain.post.dto.resp.PostRespDto;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.exception.PostErrorCode;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository;
import com.app.backend.global.config.FileConfig;
import com.app.backend.global.error.exception.GlobalErrorCode;
import com.app.backend.global.redis.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final FileConfig fileConfig;
    private final FileService fileService;
    private final PostRepository postRepository;
    private final RedisRepository redisRepository;
    private final MemberRepository memberRepository;
    private final PostAttachmentRepository postAttachmentRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    private final int MAX_FILE_SIZE = 10 * 1024 * 1024;

    public PostRespDto.GetPostDto getPost(final Long postId, final Long memberId) {
        String redisKey = "post:" + postId;
        Post post = getPostEntity(postId);
        GroupMembership membership = getMemberShipEntity(post.getGroupId(), memberId);

        if (!post.getPostStatus().equals(PostStatus.PUBLIC) && !membership.getStatus().equals(MembershipStatus.APPROVED)) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        if (redisRepository.isKeyExists(redisKey)) {
            return (PostRespDto.GetPostDto) redisRepository.get(redisKey);
        }

        Member member = getMemberEntity(memberId);

        // document
        List<PostAttachmentRespDto.GetPostDocumentDto> documents = postAttachmentRepository
                .findByPostIdAndFileTypeAndDisabledOrderByCreatedAtDesc(postId, FileType.DOCUMENT, false).stream()
                .map(PostAttachmentRespDto::getPostDocument)
                .toList();

        // image
        List<PostAttachmentRespDto.GetPostImageDto> images = postAttachmentRepository
                .findByPostIdAndFileTypeAndDisabledOrderByCreatedAtDesc(postId, FileType.IMAGE, false).stream()
                .map(file -> PostAttachmentRespDto.GetPostImage(file, fileConfig.getIMAGE_DIR()))
                .toList();

        PostRespDto.GetPostDto getPostDto = PostRespDto.toGetPost(post, member, images, documents);

        redisRepository.save(redisKey, getPostDto, 30, TimeUnit.MINUTES);

        return getPostDto;
    }


    public Page<PostRespDto.GetPostListDto> getPostsBySearch(final Long groupId, final String search, final PostStatus postStatus, final Pageable pageable) {
        return postRepository
                .findAllBySearchStatus(groupId, search, postStatus, false, pageable)
                .map(PostRespDto::toGetPostList);
    }

    public Page<PostRespDto.GetPostListDto> getPostsByUser(final PostReqDto.SearchPostDto searchPost, final Pageable pageable, final Long memberId) {
        return postRepository
                .findAllByUserAndSearchStatus(searchPost.getGroupId(), memberId, searchPost.getSearch(), searchPost.getPostStatus(), false, pageable)
                .map(PostRespDto::toGetPostList);
    }


    @Transactional
    public Post savePost(final Long memberId, final PostReqDto.SavePostDto savePost, final MultipartFile[] files) {
        GroupMembership membership = getMemberShipEntity(savePost.getGroupId(), memberId);

        if (!membership.getStatus().equals(MembershipStatus.APPROVED)) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        if (savePost.getPostStatus().equals(PostStatus.NOTICE) && !membership.getGroupRole().equals(GroupRole.LEADER)) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }
        Member member = getMemberEntity(memberId);
        Post post = postRepository.save(savePost.toEntity(memberId,member.getNickname()));

        saveFiles(files, post);

        return post;
    }


    @Transactional
    public Post updatePost(final Long memberId, final Long postId, final PostReqDto.ModifyPostDto modifyPost, final MultipartFile[] files) {
        String redisKey = "post:" + postId;
        GroupMembership membership = getMemberShipEntity(modifyPost.getGroupId(), memberId);
        Post post = getPostEntity(postId);

        if (!membership.getStatus().equals(MembershipStatus.APPROVED)) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        if (!(post.getMemberId().equals(memberId) || membership.getGroupRole().equals(GroupRole.LEADER))) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        checkFileSize(files, modifyPost.getOldFileSize(), (long) MAX_FILE_SIZE);

        post.setTitle(modifyPost.getTitle());
        post.setContent(modifyPost.getContent());
        post.setPostStatus(modifyPost.getPostStatus());

        saveFiles(files, post);

        if (modifyPost.getRemoveIdList() != null && !modifyPost.getRemoveIdList().isEmpty()) {
            postAttachmentRepository.deleteByIdList(modifyPost.getRemoveIdList());
        }

        if(redisRepository.isKeyExists(redisKey)) {
            redisRepository.delete(redisKey);
        }

        return post;
    }


    @Transactional
    public void deletePost(final Long memberId, final Long postId) {
        String redisKey = "post:" + postId;
        Post post = getPostEntity(postId);
        GroupMembership membership = getMemberShipEntity(post.getGroupId(), memberId);

        if (!membership.getStatus().equals(MembershipStatus.APPROVED)) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        if (!(post.getMemberId().equals(memberId) || membership.getGroupRole().equals(GroupRole.LEADER))) {
            throw new PostException(PostErrorCode.POST_UNAUTHORIZATION);
        }

        if(redisRepository.isKeyExists(redisKey)) {
            redisRepository.delete(redisKey);
        }

        postAttachmentRepository.deleteByPostId(postId);

        post.delete();
    }


    private Member getMemberEntity(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new PostException(GlobalErrorCode.ENTITY_NOT_FOUND));
    }


    private Post getPostEntity(final Long postId) {
        return postRepository.findByIdAndDisabled(postId, false)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
    }


    private GroupMembership getMemberShipEntity(final Long groupId, final Long memberId) {
        return groupMembershipRepository.findById(GroupMembershipId.builder().groupId(groupId).memberId(memberId).build())
                .orElseThrow(() -> new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
    }


    // 파일 크기 체크
    private void checkFileSize(final MultipartFile[] files, final Long oldSize, final Long maxSize) {
        Long newSize = files != null ? Arrays.stream(files).mapToLong(MultipartFile::getSize).sum() : 0;
        if (oldSize + newSize > maxSize) {
            throw new FileException(FileErrorCode.FILE_SIZE_EXCEEDED);
        }
    }


    // 파일 저장 및 롤백
    private void saveFiles(final MultipartFile[] files, final Post post) {
        if (files == null || files.length == 0) return;

        List<PostAttachment> attachments = new ArrayList<>();
        List<String> filePaths = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                String filePath = fileService.saveFile(file);
                String fileName = FileUtil.getFileName(filePath);
                filePaths.add(fileConfig.getBASE_DIR() + "/" + filePath);
                attachments.add(new PostAttachment(
                        file.getOriginalFilename(),
                        fileName,
                        filePath,
                        file.getSize(),
                        file.getContentType(),
                        FileUtil.getFileType(fileName),
                        post.getId()));
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