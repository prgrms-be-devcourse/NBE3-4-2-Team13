package com.app.backend.domain.attachment.controller;

import com.app.backend.domain.attachment.dto.resp.FileRespDto;
import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.domain.post.service.postAttachment.PostAttachmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostAttachmentService postAttachmentService;

    @Test
    @DisplayName("Success : 존재하는 파일 다운로드")
    void downloadFile_Success() throws Exception {
        // given
        Member member = Member.builder()
                .id(1L)
                .username("test")
                .nickname("test_name")
                .role("ROLE_USER")
                .build();

        MemberDetails mockUser = new MemberDetails(member);

        Resource mockResource =
                new ByteArrayResource("test file content".getBytes()) {
                    @Override
                    public String getFilename() {
                        return "20250204_adfasdfd.pdf";
                    }
                };

        PostAttachment mockAttachment =
                new PostAttachment(
                        "test1.pdf",
                        "20250204_adfasdfd.pdf",
                        "test/20250204_adfasdfd.pdf",
                        10L,
                        "application/pdf",
                        FileType.DOCUMENT,
                        1L);

        FileRespDto.downloadDto mockDto = FileRespDto.downloadDto.builder().resource(mockResource).attachment(mockAttachment).build();

        given(postAttachmentService.downloadFile(eq(1L), any())).willReturn(mockDto);

        // when
        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/download/post/{id}", 1L)
                        .with(user(mockUser)));

        // Then
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(header().string(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"test1.pdf\""));
        resultActions.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF));
        resultActions.andExpect(content().string("test file content"));
        resultActions.andDo(print());
    }

    @Test
    @DisplayName("Fail : 파일 존재하지 않을 경우")
    void downloadFile_Fail1() throws Exception {
        // given
        Member member = Member.builder()
                .id(1L)
                .username("test")
                .nickname("test_name")
                .role("ROLE_USER")
                .build();

        MemberDetails mockUser = new MemberDetails(member);

        given(postAttachmentService.downloadFile(eq(11L), any()))
                .willThrow(new FileException(FileErrorCode.FILE_NOT_FOUND));

        // when
        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/download/post/{id}", 11L)
                        .with(user(mockUser)));

        // Then
        resultActions.andExpect(status().isNotFound());
        resultActions.andDo(print());
    }

    @Test
    @DisplayName("Fail : 인증된 유저가 아닐경우")
    void downloadFile_Fail2() throws Exception {
        // given
        Member member = Member.builder()
                .id(1L)
                .username("test")
                .nickname("test_name")
                .role("ROLE_USER")
                .build();

        MemberDetails mockUser = new MemberDetails(member);

        given(postAttachmentService.downloadFile(eq(1L), any()))
                .willThrow(new FileException(FileErrorCode.FILE_NOT_FOUND));

        // when
        ResultActions resultActions = mockMvc
                .perform(get("/api/v1/download/post/{id}", 999L));

        // Then
        resultActions.andExpect(status().is3xxRedirection());
        resultActions.andDo(print());
    }
}

