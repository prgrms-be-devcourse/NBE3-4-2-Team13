package com.app.backend.domain.group.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.group.constant.GroupMessageConstant;
import com.app.backend.domain.group.dto.request.GroupRequest;
import com.app.backend.domain.group.dto.response.GroupResponse;
import com.app.backend.domain.group.dto.response.GroupResponse.ListInfo;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupException;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.supporter.WebMvcTestSupporter;
import com.app.backend.global.annotation.CustomPageJsonSerializer;
import com.app.backend.global.annotation.CustomWithMockUser;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.error.exception.GlobalErrorCode;
import com.app.backend.global.module.CustomPageModule;
import com.app.backend.global.util.ReflectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

class GroupControllerTest extends WebMvcTestSupporter {

    GroupResponse.Detail response;
    Page<ListInfo>       responsePage;

    @BeforeEach
    void beforeEach() {
        Category category = Category.builder()
                                    .name("category")
                                    .build();

        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .category(category)
                           .build();
        ReflectionUtil.setPrivateFieldValue(Group.class, group, "createdAt", LocalDateTime.now());

        response = GroupResponse.toDetail(group);
        responsePage = new PageImpl<>(List.of(GroupResponse.toListInfo(group)), PageRequest.of(0, 10), 1);

        when(groupService.createGroup(anyLong(), any(GroupRequest.Create.class))).thenReturn(1L);
        when(groupService.getGroup(anyLong(), anyLong())).thenReturn(response);
        when(groupService.getGroupsBySearch(any(GroupRequest.Search.class), any(Pageable.class)))
                .thenReturn(responsePage);
        when(groupService.modifyGroup(anyLong(), anyLong(), any(GroupRequest.Update.class))).thenReturn(response);
        when(groupService.deleteGroup(anyLong(), anyLong())).thenReturn(true);
        when(groupMembershipService.approveJoining(anyLong(), anyLong(), anyLong(), eq(true))).thenReturn(true);
        when(groupMembershipService.approveJoining(anyLong(), anyLong(), anyLong(), eq(false))).thenReturn(false);
        when(groupMembershipService.modifyGroupRole(anyLong(), anyLong(), anyLong())).thenReturn(true);
        when(groupMembershipService.leaveGroup(anyLong(), anyLong())).thenReturn(true);
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[성공] 신규 모임 생성")
    void createGroup() throws Exception {
        //Given
        GroupRequest.Create requestDto = GroupRequest.Create.builder()
                                                            .name("test")
                                                            .province("test province")
                                                            .city("test city")
                                                            .town("test town")
                                                            .description("test description")
                                                            .maxRecruitCount(10)
                                                            .categoryName("category")
                                                            .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        //When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/groups")
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        ApiResponse<Object> apiResponse = ApiResponse.of(true,
                                                         HttpStatus.CREATED,
                                                         GroupMessageConstant.CREATE_GROUP_SUCCESS);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("createGroup"))
                     .andExpect(status().isCreated())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[예외] 신규 모임 생성 요청 DTO에 올바르지 않은 값 존재 시")
    void createGroup_invalidValue() throws Exception {
        //Given
        GroupRequest.Create requestDto = GroupRequest.Create.builder()
                                                            .name("")
                                                            .province("     ")
                                                            .city("     ")
                                                            .town("")
                                                            .description(null)
                                                            .maxRecruitCount(-1234567890)
                                                            .categoryName(null)
                                                            .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        //When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/groups")
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        GlobalErrorCode     errorCode    = GlobalErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Object> apiResponse  = ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage());
        String              responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("createGroup"))
                     .andExpect(status().isBadRequest())
                     .andExpect(result -> {
                         assertThat(result.getResolvedException() instanceof GroupException).isTrue();
                         assertThat(result.getResolvedException().getMessage()).isEqualTo(errorCode.getMessage());
                     })
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[성공] ID로 모임 단 건 조회")
    void getGroupById() throws Exception {
        //Given

        //When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/groups/{groupId}", 1)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE));

        //Then
        ApiResponse<Object> apiResponse = ApiResponse.of(true,
                                                         HttpStatus.OK,
                                                         GroupMessageConstant.READ_GROUP_SUCCESS,
                                                         response);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("getGroupById"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[예외] 올바르지 않은 ID로 모임 단 건 조회 시도")
    void getGroupById_invalidId() throws Exception {
        //Given
        Long invalidId = -1234567890L;

        //When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/groups/{groupId}", invalidId)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE));

        //Then
        GlobalErrorCode     errorCode    = GlobalErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Object> apiResponse  = ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage());
        String              responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("getGroupById"))
                     .andExpect(status().isBadRequest())
                     .andExpect(result -> assertThat(
                             result.getResolvedException() instanceof HandlerMethodValidationException
                     ).isTrue())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[성공] 모임 목록 조회")
    void getGroups() throws Exception {
        //Given
        CustomPageJsonSerializer annotation = (CustomPageJsonSerializer) Proxy.newProxyInstance(
                CustomPageJsonSerializer.class.getClassLoader(),
                new Class[]{CustomPageJsonSerializer.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "content":
                            return true;
                        case "hasContent":
                            return false;
                        case "totalPages":
                            return true;
                        case "totalElements":
                            return true;
                        case "numberOfElements":
                            return true;
                        case "size":
                            return false;
                        case "number":
                            return true;
                        case "hasPrevious":
                            return true;
                        case "hasNext":
                            return true;
                        case "isFirst":
                            return false;
                        case "isLast":
                            return false;
                        case "sort":
                            return false;
                        case "empty":
                            return false;
                        default:
                            return method.getDefaultValue();
                    }
                }
        );

        objectMapper = new ObjectMapper();
        objectMapper.registerModules(new JavaTimeModule(), new CustomPageModule(annotation));
        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GroupRequest.Search requestDto = GroupRequest.Search.builder()
                                                            .categoryName("category")
                                                            .name("1")
                                                            .province("test province10")
                                                            .city("test city10")
                                                            .town("test town10")
                                                            .build();

        //When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/groups")
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .param("categoryName", "category")
                                                              .param("keyword", "1")
                                                              .param("province", "test province10")
                                                              .param("city", "test city10")
                                                              .param("town", "test town10")
                                                              .param("page", "0")
                                                              .param("size", "10")
                                                              .param("sort", "createdAt,DESC"));

        //Then
        ApiResponse<Page<ListInfo>> apiResponse = ApiResponse.of(true,
                                                                 HttpStatus.OK,
                                                                 GroupMessageConstant.READ_GROUPS_SUCCESS,
                                                                 responsePage);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("getGroups"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[성공] 기존 모임 수정")
    void modifyGroup() throws Exception {
        //Given
        GroupRequest.Update requestDto = GroupRequest.Update.builder()
                                                            .name("new test")
                                                            .province("new test province")
                                                            .city("new test city")
                                                            .town("new test town")
                                                            .description("new test description")
                                                            .recruitStatus("CLOSED")
                                                            .maxRecruitCount(20)
                                                            .categoryName("category")
                                                            .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        //When
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/groups/{groupId}", 1)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        ApiResponse<Object> apiResponse = ApiResponse.of(true,
                                                         HttpStatus.OK,
                                                         GroupMessageConstant.UPDATE_GROUP_SUCCESS);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("modifyGroup"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[예외] 올바르지 않은 ID로 기존 모임 수정 시도")
    void modifyGroup_invalidId() throws Exception {
        //Given
        Long invalidId = -1234567890L;

        GroupRequest.Update requestDto = GroupRequest.Update.builder()
                                                            .name("new test")
                                                            .province("new test province")
                                                            .city("new test city")
                                                            .town("new test town")
                                                            .description("new test description")
                                                            .recruitStatus("CLOSED")
                                                            .maxRecruitCount(20)
                                                            .categoryName("category")
                                                            .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        //When
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/groups/{groupId}", invalidId)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        GlobalErrorCode     errorCode    = GlobalErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Object> apiResponse  = ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage());
        String              responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("modifyGroup"))
                     .andExpect(status().isBadRequest())
                     .andExpect(result -> assertThat(
                             result.getResolvedException() instanceof HandlerMethodValidationException
                     ).isTrue())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[성공] 기존 모임 삭제(Soft Delete)")
    void deleteGroup() throws Exception {
        //Given

        //When
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/groups/{groupId}", 1)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE));

        //Then
        ApiResponse<Object> apiResponse = ApiResponse.of(true,
                                                         HttpStatus.OK,
                                                         GroupMessageConstant.DELETE_GROUP_SUCCESS);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("deleteGroup"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[예외] 올바르지 않은 ID로 기존 모임 삭제 시도")
    void deleteGroup_invalidId() throws Exception {
        //Given
        Long invalidId = -1234567890L;

        //When
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/groups/{groupId}", invalidId)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE));

        //Then
        GlobalErrorCode     errorCode    = GlobalErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Object> apiResponse  = ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage());
        String              responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("deleteGroup"))
                     .andExpect(status().isBadRequest())
                     .andExpect(result -> assertThat(
                             result.getResolvedException() instanceof HandlerMethodValidationException
                     ).isTrue())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[성공] 가입된 모임에서 탈퇴")
    void leaveGroup() throws Exception {
        //Given

        //When
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/groups/{groupId}/leave", 1)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE));

        //Then
        ApiResponse<Object> apiResponse = ApiResponse.of(true,
                                                         HttpStatus.OK,
                                                         GroupMessageConstant.LEAVE_GROUP_SUCCESS);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("leaveGroup"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[예외] 올바르지 않은 모임 ID로 탈퇴 시도")
    void leaveGroup_invalidId() throws Exception {
        //Given
        Long invalidId = -1234567890L;

        //When
        ResultActions resultActions = mockMvc.perform(delete("/api/v1/groups/{groupId}/leave", invalidId)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE));

        //Then
        GlobalErrorCode     errorCode    = GlobalErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Object> apiResponse  = ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage());
        String              responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("leaveGroup"))
                     .andExpect(status().isBadRequest())
                     .andExpect(result -> assertThat(
                             result.getResolvedException() instanceof HandlerMethodValidationException
                     ).isTrue())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[성공] 모임 관리자가 모임 가입 신청을 승인")
    void approveJoining_approve() throws Exception {
        //Given
        GroupRequest.ApproveJoining requestDto = GroupRequest.ApproveJoining.builder()
                                                                            .memberId(1L)
                                                                            .isAccept(true)
                                                                            .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        //When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/groups/{groupId}/approve", 1)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        ApiResponse<Object> apiResponse = ApiResponse.of(true,
                                                         HttpStatus.OK,
                                                         GroupMessageConstant.APPROVE_JOINING_SUCCESS);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("approveJoining"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[성공] 모임 관리자가 모임 가입 신청을 거절")
    void approveJoining_reject() throws Exception {
        //Given
        GroupRequest.ApproveJoining requestDto = GroupRequest.ApproveJoining.builder()
                                                                            .memberId(1L)
                                                                            .isAccept(false)
                                                                            .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        //When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/groups/{groupId}/approve", 1)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        ApiResponse<Object> apiResponse = ApiResponse.of(true,
                                                         HttpStatus.OK,
                                                         GroupMessageConstant.REJECT_JOINING_SUCCESS);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("approveJoining"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[예외] 올바르지 않은 모임 ID로 가입 신청 승인/거절 시도")
    void approveJoining_invalidId() throws Exception {
        //Given
        GroupRequest.ApproveJoining requestDto = GroupRequest.ApproveJoining.builder()
                                                                            .memberId(1L)
                                                                            .isAccept(true)
                                                                            .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);
        Long   invalidId   = -1234567890L;

        //When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/groups/{groupId}/approve", invalidId)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Object> apiResponse = ApiResponse.of(false,
                                                         errorCode.getCode(),
                                                         errorCode.getMessage());
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("approveJoining"))
                     .andExpect(status().isBadRequest())
                     .andExpect(result -> assertThat(
                             result.getResolvedException() instanceof HandlerMethodValidationException
                     ).isTrue())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[예외] 모임 가입 신청 승인/거절 DTO에 올바르지 않은 값 존재 시")
    void approveJoining_invalidValue() throws Exception {
        //Given
        GroupRequest.ApproveJoining requestDto = GroupRequest.ApproveJoining.builder()
                                                                            .memberId(-1234567890L)
                                                                            .isAccept(true)
                                                                            .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        //When
        ResultActions resultActions = mockMvc.perform(post("/api/v1/groups/{groupId}/approve", 1)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        GlobalErrorCode errorCode = GlobalErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Object> apiResponse = ApiResponse.of(false,
                                                         errorCode.getCode(),
                                                         errorCode.getMessage());
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("approveJoining"))
                     .andExpect(status().isBadRequest())
                     .andExpect(result -> {
                         assertThat(result.getResolvedException() instanceof GroupMembershipException).isTrue();
                         assertThat(result.getResolvedException().getMessage()).isEqualTo(errorCode.getMessage());
                     })
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[성공] 모임 관리자가 모임 내 회원의 권한을 변경")
    void modifyGroupRole() throws Exception {
        //Given
        GroupRequest.Permission requestDto = GroupRequest.Permission.builder()
                                                                    .memberId(1L)
                                                                    .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        //When
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/groups/{groupId}/permission", 1)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        ApiResponse<Object> apiResponse = ApiResponse.of(true,
                                                         HttpStatus.OK,
                                                         GroupMessageConstant.MODIFY_GROUP_ROLE_SUCCESS);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("modifyGroupRole"))
                     .andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[예외] 올바르지 않은 모임 ID로 가입 신청 승인/거절 시도")
    void modifyGroupRole_invalidId() throws Exception {
        //Given
        GroupRequest.Permission requestDto = GroupRequest.Permission.builder()
                                                                    .memberId(1L)
                                                                    .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);
        Long   invalidId   = -1234567890L;

        //When
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/groups/{groupId}/permission", invalidId)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        GlobalErrorCode     errorCode    = GlobalErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Object> apiResponse  = ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage());
        String              responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("modifyGroupRole"))
                     .andExpect(status().isBadRequest())
                     .andExpect(result -> assertThat(
                             result.getResolvedException() instanceof HandlerMethodValidationException
                     ).isTrue())
                     .andDo(print());
    }

    @Test
    @CustomWithMockUser
    @DisplayName("[예외] 모임 탈퇴 DTO에 올바르지 않은 값 존재 시")
    void modifyGroupRole_invalidValue() throws Exception {
        //Given
        GroupRequest.Permission requestDto = GroupRequest.Permission.builder()
                                                                    .memberId(-1234567890L)
                                                                    .build();
        String requestBody = objectMapper.writeValueAsString(requestDto);

        //When
        ResultActions resultActions = mockMvc.perform(patch("/api/v1/groups/{groupId}/permission", 1)
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .content(requestBody));

        //Then
        GlobalErrorCode     errorCode    = GlobalErrorCode.INVALID_INPUT_VALUE;
        ApiResponse<Object> apiResponse  = ApiResponse.of(false, errorCode.getCode(), errorCode.getMessage());
        String              responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(handler().handlerType(GroupController.class))
                     .andExpect(handler().methodName("modifyGroupRole"))
                     .andExpect(status().isBadRequest())
                     .andExpect(result -> {
                         assertThat(result.getResolvedException() instanceof GroupMembershipException).isTrue();
                         assertThat(result.getResolvedException().getMessage()).isEqualTo(errorCode.getMessage());
                     })
                     .andDo(print());
    }

}