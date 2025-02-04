package com.app.backend.domain.post.service.postAttachment;

import com.app.backend.domain.attachment.dto.resp.FileRespDto;
import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import com.app.backend.domain.group.entity.Group;
import com.app.backend.domain.group.entity.GroupMembership;
import com.app.backend.domain.group.entity.GroupRole;
import com.app.backend.domain.group.entity.RecruitStatus;
import com.app.backend.domain.group.exception.GroupMembershipErrorCode;
import com.app.backend.domain.group.exception.GroupMembershipException;
import com.app.backend.domain.group.repository.GroupMembershipRepository;
import com.app.backend.domain.group.repository.GroupRepository;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.dto.req.PostReqDto;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.exception.PostErrorCode;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository;
import com.app.backend.domain.post.service.post.PostService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PostAttachmentServiceTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private PostService postService;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostAttachmentService postAttachmentService;

    @Autowired
    private PostAttachmentRepository postAttachmentRepository;

    @Autowired
    private GroupMembershipRepository groupMembershipRepository;

    private static String BASE_DIR;

    @BeforeAll
    static void setUpAll(@Value("${spring.file.base-dir}") String baseDir) {
        BASE_DIR = baseDir;
    }

    @BeforeEach
    void setUp() {
        autoIncrementReset();
        dataSetting();
        em.flush();
        em.clear();
    }

    private void autoIncrementReset() {
        em.createNativeQuery("ALTER TABLE tbl_posts ALTER COLUMN post_id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE tbl_members ALTER COLUMN member_id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE tbl_groups ALTER COLUMN group_id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE tbl_post_attachments ALTER COLUMN attachment_id RESTART WITH 1").executeUpdate();
    }

    private void dataSetting() {
        Member member1 = memberRepository.save(Member.builder().username("Test member1").nickname("Test Nickname 1").build());
        Member member2 = memberRepository.save(Member.builder().username("Test member2").nickname("Test Nickname 2").build());

        Group group = groupRepository.save(Group.builder()
                .name("test")
                .province("test province")
                .city("test city")
                .town("test town")
                .description("test description")
                .recruitStatus(RecruitStatus.RECRUITING)
                .maxRecruitCount(10)
                .build());

        groupMembershipRepository.save(GroupMembership.builder().member(member1).group(group).groupRole(GroupRole.LEADER).build());
        groupMembershipRepository.save(GroupMembership.builder().member(member2).group(group).groupRole(GroupRole.PARTICIPANT).build());

        MultipartFile[] files1 = {new MockMultipartFile("test1", "test1.pdf", "application/pdf", "pdf-file-content".getBytes())};
        MultipartFile[] files2 = {new MockMultipartFile("test2", "test2.pdf", "application/pdf", "pdf-file-content".getBytes())};

        PostReqDto.SavePostDto savePostDto1 = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L);
        PostReqDto.SavePostDto savePostDto2 = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PRIVATE, 1L);

        postService.savePost(1L, savePostDto1, files1);
        postService.savePost(1L, savePostDto2, files2);
    }

    @Test
    @DisplayName("Success : 파일이 정상적으로 존재하는 경우")
    void downloadFile_Success1() {
        // when
        FileRespDto.downloadDto downloadFile = postAttachmentService.downloadFile(1L,1L);

        // Then
        assertNotNull(downloadFile.getResource());
        assertEquals("test1.pdf", downloadFile.getAttachment().getOriginalFileName());
        assertEquals("application/pdf", downloadFile.getAttachment().getContentType());
    }

    @Test
    @DisplayName("Success : public 게시물 - 그룹 멤버 x")
    void downloadFile_Success2() {
        // when
        FileRespDto.downloadDto downloadFile = postAttachmentService.downloadFile(1L,2L);

        // Then
        assertNotNull(downloadFile.getResource());
        assertEquals("test1.pdf", downloadFile.getAttachment().getOriginalFileName());
        assertEquals("application/pdf", downloadFile.getAttachment().getContentType());
    }

    @Test
    @DisplayName("Success : private 게시물 - 그룹 멤버 O")
    void downloadFile_Success3() {
        // when
        FileRespDto.downloadDto downloadFile = postAttachmentService.downloadFile(2L,1L);

        // Then
        assertNotNull(downloadFile.getResource());
        assertEquals("test2.pdf", downloadFile.getAttachment().getOriginalFileName());
        assertEquals("application/pdf", downloadFile.getAttachment().getContentType());
    }

    @Test
    @DisplayName("Fail : private 게시물 - 그룹 멤버 x")
    void downloadFile_Fail1() {
        // Then
        assertThatThrownBy(() -> postAttachmentService.downloadFile(2L,2L))
                .isInstanceOf(GroupMembershipException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND)
                .hasMessage(GroupMembershipErrorCode.GROUP_MEMBERSHIP_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Fail : DB에 파일 정보가 없을 경우")
    void downloadFile_Fail2() {
        // Then
        assertThatThrownBy(() -> postAttachmentService.downloadFile(11L,1L))
                .isInstanceOf(FileException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", FileErrorCode.FILE_NOT_FOUND)
                .hasMessage(FileErrorCode.FILE_NOT_FOUND.getMessage());
    }

    @AfterAll
    public static void tearDown() {
        deleteTestUploadsFile();
    }

    private static void deleteTestUploadsFile() {
        File folder = new File(BASE_DIR);
        if (folder.exists()) {
            deleteFolderRecursively(folder); // 폴더 내부 파일까지 삭제
            folder.delete(); // 폴더 자체 삭제
        }
    }

    private static void deleteFolderRecursively(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolderRecursively(file);
                }
                file.delete();
            }
        }
    }
}
