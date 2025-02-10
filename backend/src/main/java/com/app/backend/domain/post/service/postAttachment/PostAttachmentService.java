package com.app.backend.domain.post.service.postAttachment;

import com.app.backend.domain.attachment.dto.resp.FileRespDto;
import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupMembershipId;
import com.app.backend.domain.group.entity.MembershipStatus;
import com.app.backend.domain.group.exception.GroupMembershipErrorCode;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.exception.PostErrorCode;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository;
import com.app.backend.global.config.FileConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostAttachmentService {

    private final FileConfig fileConfig;
    private final PostRepository postRepository;
    private final PostAttachmentRepository postAttachmentRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public FileRespDto.downloadDto downloadFile(final Long attachmentId, final Long memberId) {

        PostAttachment file = getPostAttachment(attachmentId);

        Post post = getPostEntity(file.getPostId());

        if (!post.getPostStatus().equals(PostStatus.PUBLIC) && !getMemberShipEntity(post.getGroupId(), memberId).getStatus().equals(MembershipStatus.APPROVED)) {
            throw new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND);
        }

        try {
            Path filePath = Paths.get(fileConfig.getBASE_DIR(), file.getStoreFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isFile()) {
                throw new FileException(FileErrorCode.FILE_NOT_FOUND);
            }

            return FileRespDto.downloadDto.builder().resource(resource).attachment(file).build();
        } catch (Exception e) {
            throw new FileException(FileErrorCode.FILE_NOT_FOUND);
        }
    }

    public PostAttachment getPostAttachment(final Long attachmentId) {
        return postAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new FileException(FileErrorCode.FILE_NOT_FOUND));
    }

    private Post getPostEntity(final Long postId) {
        return postRepository.findByIdAndDisabled(postId, false)
                .orElseThrow(() -> new PostException(PostErrorCode.POST_NOT_FOUND));
    }

    private GroupMembership getMemberShipEntity(final Long groupId, final Long memberId) {
        return groupMembershipRepository.findById(GroupMembershipId.builder().groupId(groupId).memberId(memberId).build())
                .orElseThrow(() -> new GroupMembershipException(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND));
    }
}
