package com.app.backend.domain.attachment.controller;

import com.app.backend.domain.attachment.dto.resp.FileRespDto;
import com.app.backend.domain.member.entity.MemberDetails;
import com.app.backend.domain.post.service.postAttachment.PostAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/download")
public class FileController {

    private final PostAttachmentService postAttachmentService;

    @GetMapping("/post/{id}")
    public ResponseEntity<?> downloadFile(
            @PathVariable final Long id,
            @AuthenticationPrincipal final MemberDetails memberDetails
    ) {

        FileRespDto.downloadDto downloadFile = postAttachmentService.downloadFile(id, memberDetails.getId());

        String contentType = downloadFile.getAttachment().getContentType();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFile.getAttachment().getOriginalFileName() + "\"")
                .contentType(contentType == null ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType))
                .body(downloadFile.getResource());
    }

}
