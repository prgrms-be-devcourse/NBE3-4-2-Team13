package com.app.backend.domain.post.repository.post;

import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.entity.QPost;
import com.app.backend.global.error.exception.DomainException;
import com.app.backend.global.error.exception.GlobalErrorCode;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Post> findAllBySearchStatus(final Long groupId, final String search, final PostStatus postStatus, final boolean disabled, final Pageable pageable) {

        QPost post = QPost.post;

        List<Post> posts = jpaQueryFactory.selectFrom(post)
                .where(searchKeywordContains(post, search),
                        checkPostStatus(post, postStatus),
                        post.groupId.eq(groupId),
                        post.disabled.eq(disabled))
                .orderBy(getSortCondition(pageable, post))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                jpaQueryFactory.select(post.count())
                        .from(post)
                        .where(searchKeywordContains(post, search),
                                checkPostStatus(post, postStatus),
                                post.groupId.eq(groupId),
                                post.disabled.eq(disabled))
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(posts, pageable, total);
    }

    @Override
    public Page<Post> findAllByUserAndSearchStatus(final Long groupId, final Long memberId, final String search, final PostStatus postStatus, final boolean disabled, final Pageable pageable) {
        QPost post = QPost.post;

        List<Post> posts = jpaQueryFactory.selectFrom(post)
                .where(searchKeywordContains(post, search),
                        checkPostStatus(post, postStatus),
                        post.groupId.eq(groupId),
                        post.memberId.eq(memberId),
                        post.disabled.eq(disabled))
                .orderBy(getSortCondition(pageable, post))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(
                jpaQueryFactory.select(post.count())
                        .from(post)
                        .where(searchKeywordContains(post, search),
                                checkPostStatus(post, postStatus),
                                post.groupId.eq(groupId),
                                post.memberId.eq(memberId),
                                post.disabled.eq(disabled))
                        .fetchOne()
        ).orElse(0L);

        return new PageImpl<>(posts, pageable, total);
    }

    private BooleanExpression searchKeywordContains(final QPost post, final String search) {
        return (search == null || search.isEmpty()) ? null : post.title.containsIgnoreCase(search);
    }

    private BooleanExpression checkPostStatus(final QPost post, final PostStatus postStatus) {
        return (postStatus == PostStatus.ALL) ? null : post.postStatus.eq(postStatus);
    }

    private boolean isValidColumn(String column) {
        List<String> validColumns = Arrays.asList("title", "createdAt", "modifiedAt");
        return validColumns.contains(column);
    }

    private OrderSpecifier<?>[] getSortCondition(Pageable pageable, QPost post) {

        if (pageable.getSort().isEmpty()) {
            return new OrderSpecifier[]{post.createdAt.desc()};
        }

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            String column = order.getProperty();
            if (!isValidColumn(column)) {
                throw new DomainException(GlobalErrorCode.INVALID_INPUT_VALUE);
            }

            Expression<?> path = Expressions.path(Comparable.class, post, column);
            orders.add(new OrderSpecifier(order.isAscending() ? Order.ASC : Order.DESC, path));
        }

        return orders.toArray(new OrderSpecifier[0]); // 첫 번째 정렬 반환
    }
}