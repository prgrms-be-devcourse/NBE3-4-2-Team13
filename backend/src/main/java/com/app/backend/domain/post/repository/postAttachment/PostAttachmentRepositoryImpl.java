package com.app.backend.domain.post.repository.postAttachment;

import com.app.backend.domain.post.entity.QPostAttachment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostAttachmentRepositoryImpl implements PostAttachmentRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void deleteByIdList(List<Long> idList) {
        QPostAttachment postAttachment = QPostAttachment.postAttachment;
        jpaQueryFactory
                .update(postAttachment)
                .set(postAttachment.disabled, true)
                .where(postAttachment.id.in(idList)
                        .and(postAttachment.disabled.eq(false)))
                .execute();
    }

    @Override
    public void deleteByPostId(Long postId) {
        QPostAttachment postAttachment = QPostAttachment.postAttachment;
        jpaQueryFactory
                .update(postAttachment)
                .set(postAttachment.disabled, true)
                .where(postAttachment.postId.eq(postId)
                        .and(postAttachment.disabled.eq(false)))
                .execute();
    }
}
