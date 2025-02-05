package com.app.backend.domain.group.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class GroupRequest {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Create {
        @NotNull
        @NotBlank
        private String  name;
        @NotNull
        @NotBlank
        private String  province;
        @NotNull
        @NotBlank
        private String  city;
        @NotNull
        @NotBlank
        private String  town;
        @NotNull
        @NotBlank
        private String  description;
        @NotNull
        @Min(1)
        private Integer maxRecruitCount;
        @NotNull
        @NotBlank
        private String  categoryName;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Update {
        @NotNull
        @NotBlank
        private String  name;
        @NotNull
        @NotBlank
        private String  province;
        @NotNull
        @NotBlank
        private String  city;
        @NotNull
        @NotBlank
        private String  town;
        @NotNull
        @NotBlank
        private String  description;
        @NotNull
        @NotBlank
        private String  recruitStatus;
        @NotNull
        @Min(1)
        private Integer maxRecruitCount;
        @NotNull
        @NotBlank
        private String  categoryName;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ApproveJoining {
        @NotNull
        @Min(1)
        private Long    memberId;
        @NotNull
        private Boolean isAccept;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Permission {
        @NotNull
        @Min(1)
        private Long memberId;
    }

}
