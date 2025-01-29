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
                .delete(postAttachment)
                .where(postAttachment.id.in(idList))
                .execute();
    }

}
