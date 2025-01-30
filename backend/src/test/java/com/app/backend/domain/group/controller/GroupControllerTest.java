package com.app.backend.domain.group.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.app.backend.domain.group.constant.GroupMessageConstant;
import com.app.backend.domain.group.dto.request.GroupRequest;
import com.app.backend.domain.group.dto.response.GroupResponse;
import com.app.backend.domain.group.dto.response.GroupResponse.ListInfo;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupException;
import com.app.backend.domain.group.supporter.WebMvcTestSupporter;
import com.app.backend.global.dto.response.ApiResponse;
import com.app.backend.global.error.exception.GlobalErrorCode;
import com.app.backend.global.util.ReflectionUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

class GroupControllerTest extends WebMvcTestSupporter {

    GroupResponse.Detail         response;
    List<GroupResponse.ListInfo> responseList;

    @BeforeEach
    void beforeEach() {
        Group group = Group.builder()
                           .name("test")
                           .province("test province")
                           .city("test city")
                           .town("test town")
                           .description("test description")
                           .recruitStatus(RecruitStatus.RECRUITING)
                           .maxRecruitCount(10)
                           .build();
        ReflectionUtil.setPrivateFieldValue(Group.class, group, "createdAt", LocalDateTime.now());

        response = GroupResponse.toDetail(group);
        responseList = List.of(GroupResponse.toListInfo(group));

        when(groupService.createGroup(any(GroupRequest.Create.class))).thenReturn(1L);
        when(groupService.getGroup(anyLong())).thenReturn(response);
        when(groupService.getGroups()).thenReturn(responseList);
        when(groupService.modifyGroup(any(GroupRequest.Update.class))).thenReturn(response);
        when(groupService.deleteGroup(anyLong())).thenReturn(true);
    }

    @Test
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

        resultActions.andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
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

        resultActions.andExpect(status().isBadRequest())
                     .andExpect(result -> {
                         assertThat(result.getResolvedException() instanceof GroupException).isTrue();
                         assertThat(result.getResolvedException().getMessage()).isEqualTo(errorCode.getMessage());
                     })
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
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

        resultActions.andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
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

        resultActions.andExpect(status().isBadRequest())
                     .andExpect(result -> assertThat(
                             result.getResolvedException() instanceof HandlerMethodValidationException).isTrue()
                     )
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
    @DisplayName("[성공] 모임 목록 조회")
    void getGroups() throws Exception {
        //Given

        //When
        ResultActions resultActions = mockMvc.perform(get("/api/v1/groups")
                                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                                              .contentType(MediaType.APPLICATION_JSON_VALUE));

        //Then
        ApiResponse<List<ListInfo>> apiResponse = ApiResponse.of(true,
                                                                 HttpStatus.OK,
                                                                 GroupMessageConstant.READ_GROUPS_SUCCESS,
                                                                 responseList);
        String responseBody = objectMapper.writeValueAsString(apiResponse);

        resultActions.andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
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

        resultActions.andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
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

        resultActions.andExpect(status().isBadRequest())
                     .andExpect(result -> assertThat(
                             result.getResolvedException() instanceof HandlerMethodValidationException).isTrue()
                     )
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
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

        resultActions.andExpect(status().isOk())
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

    @Test
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

        resultActions.andExpect(status().isBadRequest())
                     .andExpect(result -> assertThat(
                             result.getResolvedException() instanceof HandlerMethodValidationException).isTrue()
                     )
                     .andExpect(content().json(responseBody))
                     .andDo(print());
    }

}