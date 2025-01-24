package com.app.backend.domain.group.entity;

import com.app.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "tbl_groups")
@Getter
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

    @Builder(access = AccessLevel.PRIVATE)
    private Group(@NonNull @NotBlank final String name,
                  @NonNull @NotBlank final String province,
                  @NonNull @NotBlank final String city,
                  @NonNull @NotBlank final String town,
                  @NonNull @NotBlank final String description,
                  @NonNull final RecruitStatus recruitStatus,
                  @NonNull @NotBlank @Min(1) final Integer maxRecruitCount) {
        this.name = name;
        this.province = province;
        this.city = city;
        this.town = town;
        this.description = description;
        this.recruitStatus = recruitStatus;
        this.maxRecruitCount = maxRecruitCount;
    }

    public static Group of(@NonNull @NotBlank final String name,
                           @NonNull @NotBlank final String province,
                           @NonNull @NotBlank final String city,
                           @NonNull @NotBlank final String town,
                           @NonNull @NotBlank final String description,
                           @NonNull final RecruitStatus recruitStatus,
                           @NonNull @Min(1) final Integer maxRecruitCount) {
        return Group.builder()
                    .name(name)
                    .province(province)
                    .city(city)
                    .town(town)
                    .description(description)
                    .recruitStatus(recruitStatus)
                    .maxRecruitCount(maxRecruitCount)
                    .build();
    }

    //============================== 모임(Group) 수정 메서드 ==============================//

    /**
     * 모임명 수정
     *
     * @param newName - 새로운 모임 이름
     * @return this
     */
    public Group modifyName(@NonNull @NotBlank final String newName) {
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
    public Group modifyDescription(@NonNull @NotBlank final String newDescription) {
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
    public Group modifyRecruitStatus(@NonNull final RecruitStatus newRecruitStatus) {
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
    public Group modifyMaxRecruitCount(@NonNull @Min(1) final Integer newMaxRecruitCount) {
        if (!maxRecruitCount.equals(newMaxRecruitCount))
            maxRecruitCount = newMaxRecruitCount;
        return this;
    }

    /**
     * 모임 삭제(Soft Delete)
     */
    public void delete() {
        if (!this.getDisabled())
            deactivate();
    }

}
