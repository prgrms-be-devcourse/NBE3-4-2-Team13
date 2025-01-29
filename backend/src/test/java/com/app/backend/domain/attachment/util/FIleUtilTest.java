package com.app.backend.domain.attachment.util;

import com.app.backend.domain.attachment.entity.FileType;
import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class FIleUtilTest {

    private static final Logger log = LoggerFactory.getLogger(FIleUtilTest.class);

    @Test
    @DisplayName("Success : filename 에서 확장자 추출")
    public void getSuccessExtTest() {

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test1.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        String ext = FileUtil.getExtension(mockFile.getOriginalFilename());

        Assertions.assertEquals("jpg", ext);
    }

    @Test
    @DisplayName("Fail : filename 에 확장자 없음")
    public void getFailExtTest() {

        // given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test1",
                "image/jpeg",
                "test".getBytes()
        );

        // then
        assertThatThrownBy(() -> FileUtil.getExtension(mockFile.getOriginalFilename()))
                // then
                .isInstanceOf(FileException.class)
                .hasMessageContaining(FileErrorCode.INVALID_FILE_EXTENSION.getMessage());
    }

    @Test
    @DisplayName("Success : 이미지 타입")
    public void getSuccessImgTypeTest() {
        String image = ".jpg";
        FileType fileType = FileUtil.getFileType(image);
        Assertions.assertEquals(FileType.IMAGE, fileType);
    }

    @Test
    @DisplayName("Success : 비디오 타입")
    public void getSuccessVideoTypeTest() {
        String video = ".mp4";
        FileType fileType = FileUtil.getFileType(video);
        Assertions.assertEquals(FileType.VIDEO, fileType);
    }

    @Test
    @DisplayName("Success : 문서 타입")
    public void getSuccessDocTypeTest() {
        String document = ".doc";
        FileType fileType = FileUtil.getFileType(document);
        Assertions.assertEquals(FileType.DOCUMENT, fileType);
    }

    @Test
    @DisplayName("Fail : 지원하지 않는 파일형식")
    public void getFailTypeTest() {
        String type = ".fail";
        assertThatThrownBy(() -> FileUtil.getFileType(type))
                .isInstanceOf(FileException.class)
                .hasMessageContaining(FileErrorCode.UNSUPPORTED_FILE_TYPE.getMessage());
    }
}
