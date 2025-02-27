package com.app.backend.domain.post.entity;

import com.app.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Table(name = "tbl_posts")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @Column(name = "post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String title;

    @Setter
    @Column(nullable = false)
    private String content;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus postStatus;

    @Setter
    @Column(nullable = false)
    private Long memberId;

    @Setter
    @Column(nullable = false)
    private String nickName;

//    Member Entity 연관관계
//    @JoinColumn(name = "member_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Member member;

    @Setter
    @Column(nullable = false)
    private Long groupId;

//    Group Entity 연관관계
//    @JoinColumn(name = "group_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Group group;

    @Builder.Default
    @Column(nullable = false)
    private Long todayViewCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long totalViewCount = 0L;

    public void addTodayViewCount(Long viewCount) {
        this.todayViewCount += viewCount;
    }

    public void refreshViewCount() {
        totalViewCount += todayViewCount;
        todayViewCount = 0L;
    }

    public void delete(){
        if(!this.getDisabled()){
            deactivate();
        }
    }

}
