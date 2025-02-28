package com.app.backend.domain.post.repository.postAttachment;

import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.domain.post.entity.QPostAttachment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostAttachmentRepositoryImpl implements PostAttachmentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<PostAttachment> findAllByModifiedAtAndDisabled(final LocalDateTime lastModified, final boolean disabled) {
        QPostAttachment postAttachment = QPostAttachment.postAttachment;
        return jpaQueryFactory
                .selectFrom(postAttachment)
                .where(postAttachment.disabled.eq(disabled),
                        postAttachment.modifiedAt.loe(lastModified))
                .fetch();
    }

    @Override
    public void deleteByIdList(final List<Long> idList) {
        QPostAttachment postAttachment = QPostAttachment.postAttachment;
        jpaQueryFactory
                .update(postAttachment)
                .set(postAttachment.disabled, true)
                .where(postAttachment.id.in(idList)
                        .and(postAttachment.disabled.eq(false)))
                .execute();
    }

    @Override
    public void deleteByPostId(final Long postId) {
        QPostAttachment postAttachment = QPostAttachment.postAttachment;
        jpaQueryFactory
                .update(postAttachment)
                .set(postAttachment.disabled, true)
                .where(postAttachment.postId.eq(postId)
                        .and(postAttachment.disabled.eq(false)))
                .execute();
    }

    @Override
    public void deleteByFileIdList(final List<Long> fileIdList) {
        QPostAttachment postAttachment = QPostAttachment.postAttachment;
        jpaQueryFactory
                .delete(postAttachment)
                .where(postAttachment.postId.in(fileIdList))
                .execute();
    }
}
