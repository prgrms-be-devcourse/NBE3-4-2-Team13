package com.app.backend.domain.group.controller;

import com.app.backend.domain.group.constant.GroupMessageConstant;
import com.app.backend.domain.group.dto.request.GroupRequest;
import com.app.backend.domain.group.dto.response.GroupResponse;
import com.app.backend.domain.group.exception.GroupException;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.service.GroupMembershipService;
import com.app.backend.domain.group.service.GroupService;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.error.exception.GlobalErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/groups",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class GroupController {

    private final GroupService           groupService;
    private final GroupMembershipService groupMembershipService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createGroup(@RequestBody @Valid final GroupRequest.Create requestDto,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal final UserDetails userDetails) {
        if (bindingResult.hasErrors())
            throw new GroupException(GlobalErrorCode.INVALID_INPUT_VALUE);

        Long createdGroupId = groupService.createGroup(((MemberDetails) userDetails).getId(), requestDto);

        if (createdGroupId == null)
            throw new GroupException(GlobalErrorCode.INTERNAL_SERVER_ERROR);

        return ApiResponse.of(true, HttpStatus.CREATED, GroupMessageConstant.CREATE_GROUP_SUCCESS);
    }

    @GetMapping("/{groupId}")
    public ApiResponse<GroupResponse.Detail> getGroupById(@PathVariable @Min(1) final Long groupId) {
        GroupResponse.Detail responseDto = groupService.getGroup(groupId);
        return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.READ_GROUP_SUCCESS, responseDto);
    }

    @GetMapping
    public ApiResponse<List<GroupResponse.ListInfo>> getGroups() {
        List<GroupResponse.ListInfo> responseList = groupService.getGroups();
        return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.READ_GROUPS_SUCCESS, responseList);
    }

    @PatchMapping("/{groupId}")
    public ApiResponse<Void> modifyGroup(@PathVariable @Min(1) final Long groupId,
                                         @RequestBody @Valid final GroupRequest.Update requestDto,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal final UserDetails userDetails) {
        if (bindingResult.hasErrors())
            throw new GroupException(GlobalErrorCode.INVALID_INPUT_VALUE);

        GroupResponse.Detail updatedGroupDetail = groupService.modifyGroup(groupId,
                                                                           ((MemberDetails) userDetails).getId(),
                                                                           requestDto);

        return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.UPDATE_GROUP_SUCCESS);
    }

    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> deleteGroup(@PathVariable @Min(1) final Long groupId,
                                         @AuthenticationPrincipal final UserDetails userDetails) {
        boolean flag = groupService.deleteGroup(groupId, ((MemberDetails) userDetails).getId());

        if (!flag)
            throw new GroupException(GlobalErrorCode.INTERNAL_SERVER_ERROR);

        return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.DELETE_GROUP_SUCCESS);
    }

    @DeleteMapping("/{groupId}/leave")
    public ApiResponse<Void> leaveGroup(@PathVariable @Min(1) final Long groupId,
                                        @AuthenticationPrincipal final UserDetails userDetails) {
        boolean flag = groupMembershipService.leaveGroup(groupId, ((MemberDetails) userDetails).getId());

        if (!flag)
            throw new GroupMembershipException(GlobalErrorCode.INTERNAL_SERVER_ERROR);

        return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.LEAVE_GROUP_SUCCESS);
    }

    @PostMapping("/{groupId}/approve")
    public ApiResponse<Void> approveJoining(@PathVariable @Min(1) final Long groupId,
                                            @RequestBody @Valid final GroupRequest.ApproveJoining requestDto,
                                            BindingResult bindingResult,
                                            @AuthenticationPrincipal final UserDetails userDetails) {
        if (bindingResult.hasErrors())
            throw new GroupMembershipException(GlobalErrorCode.INVALID_INPUT_VALUE);

        boolean flag = groupMembershipService.approveJoining(((MemberDetails) userDetails).getId(),
                                                             groupId,
                                                             requestDto.getMemberId(),
                                                             requestDto.getIsAccept());

        if (flag)
            return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.APPROVE_JOINING_SUCCESS);
        else
            return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.REJECT_JOINING_SUCCESS);
    }

    @PatchMapping("/{groupId}/permission")
    public ApiResponse<Void> modifyGroupRole(@PathVariable @Min(1) final Long groupId,
                                             @RequestBody @Valid final GroupRequest.Permission requestDto,
                                             BindingResult bindingResult,
                                             @AuthenticationPrincipal final UserDetails userDetails) {
        if (bindingResult.hasErrors())
            throw new GroupMembershipException(GlobalErrorCode.INVALID_INPUT_VALUE);

        boolean flag = groupMembershipService.modifyGroupRole(((MemberDetails) userDetails).getId(),
                                                              groupId,
                                                              requestDto.getMemberId());

        if (!flag)
            throw new GroupMembershipException(GlobalErrorCode.INTERNAL_SERVER_ERROR);

        return ApiResponse.of(true, HttpStatus.OK, GroupMessageConstant.MODIFY_GROUP_ROLE_SUCCESS);
    }

}
