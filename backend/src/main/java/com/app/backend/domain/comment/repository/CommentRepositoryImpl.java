package com.app.backend.domain.comment.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.app.backend.domain.comment.dto.response.CommentResponse;
import com.app.backend.domain.comment.entity.QComment;
import com.app.backend.domain.comment.entity.QCommentLike;
import com.app.backend.domain.post.entity.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<CommentResponse.CommentList> findCommentsWithLikeCount(Post post, Pageable pageable) {
		QComment comment = QComment.comment;
		QCommentLike commentLike = QCommentLike.commentLike;

		List<CommentResponse.CommentList> results = queryFactory
			.select(comment, commentLike.count())
			.from(comment)
			.leftJoin(comment.member).fetchJoin()
			.leftJoin(commentLike)
			.on(commentLike.comment.eq(comment)
				.and(commentLike.disabled.eq(false)))
			.where(comment.post.eq(post)
				.and(comment.disabled.eq(false))
				.and(comment.parent.isNull()))
			.groupBy(comment)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(comment.createdAt.desc())
			.fetch()
			.stream()
			.map(tuple -> CommentResponse.CommentList.from(
				Objects.requireNonNull(tuple.get(comment)),
				Optional.ofNullable(tuple.get(1, Number.class))
					.map(Number::longValue)
					.orElse(0L)))
			.collect(Collectors.toList());

		Long total = Optional.ofNullable(
			queryFactory
				.select(comment.count())
				.from(comment)
				.where(comment.post.eq(post)
					.and(comment.disabled.eq(false))
					.and(comment.parent.isNull()))
				.fetchOne()
		).orElse(0L);

		return new PageImpl<>(results, pageable, total);
	}
}
