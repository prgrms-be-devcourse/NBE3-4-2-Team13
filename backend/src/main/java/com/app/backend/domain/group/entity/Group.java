package com.app.backend.domain.group.entity;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.chat.room.entity.ChatRoom;
import com.app.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tbl_groups")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id", nullable = false, updatable = false)
    private Long id;    //PK

    @Column(nullable = false)
    private String name;    //모임명

    @Column(nullable = false)
    private String province;    //활동 지역: 시/도

    @Column(nullable = false)
    private String city;    //활동 지역: 시/군/구

    @Column(nullable = false)
    private String town;    //활동 지역: 읍/면/동

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; //모임 정보

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecruitStatus recruitStatus;    //모집 상태: RECRUITING, CLOSED

    @Column(nullable = false)
    @Min(1)
    private Integer maxRecruitCount;    //모임 최대 인원

    @OneToMany(mappedBy = "group")
    private List<GroupMembership> members = new ArrayList<>();  //모임 내 회원(다대다 연관관계 중간 테이블)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    @Setter(AccessLevel.PUBLIC)
    private ChatRoom chatRoom;      // 채팅방

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;  //카테고리

    @OneToMany(mappedBy = "group")
    private List<GroupLike> likes = new ArrayList<>(); // 좋아요 리스트

    @Column
    private int likeCount = 0; // 좋아요 수

    @Builder
    private Group(@NotNull final Long id,
                  @NotNull final String name,
                  @NotNull final String province,
                  @NotNull final String city,
                  @NotNull final String town,
                  @NotNull final String description,
                  @NotNull final RecruitStatus recruitStatus,
                  @NotNull @Min(1) final Integer maxRecruitCount,
                  final ChatRoom chatRoom,
                  @NotNull final Category category) {
        this.id = id;
        this.name = name;
        this.province = province;
        this.city = city;
        this.town = town;
        this.description = description;
        this.recruitStatus = recruitStatus;
        this.maxRecruitCount = maxRecruitCount;
        this.chatRoom = chatRoom;
        if (category != null)
            setRelationshipWithCategory(category);
    }

    //============================== 연관관계 메서드 ==============================//

    private void setRelationshipWithCategory(@NotNull final Category category) {
        this.category = category;
        category.getGroups().add(this);
    }

    //============================== 모임(Group) 수정 메서드 ==============================//

    /**
     * 모임명 수정
     *
     * @param newName - 새로운 모임 이름
     * @return this
     */
    public Group modifyName(@NotNull @NotBlank final String newName) {
        if (!name.equals(newName))
            name = newName;
        return this;
    }

    /**
     * 모임 활동 지역 수정
     *
     * @param newProvince - 새로운 모임 활동 지역: 시/도
     * @param newCity     - 새로운 모임 활동 지역: 시/군/구
     * @param newTown     - 새로운 모임 활동 지역: 읍/면/동
     * @return this
     */
    public Group modifyRegion(@NotNull @NotBlank final String newProvince,
                              @NotNull @NotBlank final String newCity,
                              @NotNull @NotBlank final String newTown) {
        if (!province.equals(newProvince))
            province = newProvince;
        if (!city.equals(newCity))
            city = newCity;
        if (!town.equals(newTown))
            town = newTown;
        return this;
    }

    /**
     * 모임 정보 수정
     *
     * @param newDescription - 새로운 모임 정보
     * @return this
     */
    public Group modifyDescription(@NotNull @NotBlank final String newDescription) {
        if (!description.equals(newDescription))
            description = newDescription;
        return this;
    }

    /**
     * 모집 상태 수정
     *
     * @param newRecruitStatus - 새로운 모집 상태
     * @return this
     */
    public Group modifyRecruitStatus(@NotNull final RecruitStatus newRecruitStatus) {
        if (recruitStatus != newRecruitStatus)
            recruitStatus = newRecruitStatus;
        return this;
    }

    /**
     * 최대 모집 인원 수정
     *
     * @param newMaxRecruitCount - 새로운 최대 모집 인원
     * @return this
     */
    public Group modifyMaxRecruitCount(@NotNull @Min(1) final Integer newMaxRecruitCount) {
        if (!maxRecruitCount.equals(newMaxRecruitCount))
            maxRecruitCount = newMaxRecruitCount;
        return this;
    }

    /**
     * 카테고리 수정
     *
     * @param newCategory - 새로운 카테고리
     * @return this
     */
    public Group modifyCategory(@NotNull final Category newCategory) {
        if (!category.getName().equals(newCategory.getName()))
            category = newCategory;
        return this;
    }

    /**
     * 모임 삭제(Soft Delete)
     */
    public void delete() {
        if (!this.getDisabled())
            deactivate();
    }

    /**
     * 좋아요 수 증가
     */
    public void increaseLikeCount() {
        this.likeCount++;
    }

    /**
     * 좋아요 수 감소
     */
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

}
