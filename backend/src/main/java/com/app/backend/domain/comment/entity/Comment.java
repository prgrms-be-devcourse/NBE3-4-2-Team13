package com.app.backend.domain.comment.entity;

import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.global.entity.BaseEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tbl_comments")
public class Comment extends BaseEntity {

	@Id
	@Column(name = "comment_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;


	@Column(nullable = false)
	private String content;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private Post post;

	@ManyToOne
	@JoinColumn(name = "member_id")
	private Member member;


	public void delete() {
		this.deactivate();
	}



}
