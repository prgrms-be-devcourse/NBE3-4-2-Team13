package com.app.backend.domain.post.service;

import com.app.backend.domain.attachment.exception.FileErrorCode;
import com.app.backend.domain.attachment.exception.FileException;
import com.app.backend.domain.attachment.service.FileService;
import com.app.backend.domain.member.entity.Member;
import com.app.backend.domain.member.repository.MemberRepository;
import com.app.backend.domain.post.dto.req.PostReqDto;
import com.app.backend.domain.post.dto.resp.PostRespDto;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.domain.post.entity.PostStatus;
import com.app.backend.domain.post.exception.PostErrorCode;
import com.app.backend.domain.post.exception.PostException;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository;
import com.app.backend.global.error.exception.DomainException;
import com.app.backend.global.error.exception.GlobalErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PostServiceTest {
    @Autowired
    private EntityManager em;

    @Autowired
    private PostService postService;

    @Autowired
    private FileService fileService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PostAttachmentRepository postAttachmentRepository;

    @Value("${spring.file.base-dir}")
    private static String BASE_DIR;

    @BeforeAll
    public static void setUp(@Value("${spring.file.base-dir}") String baseDir) {
        BASE_DIR = baseDir;
    }

    private void autoIncrementReset() {
        em.createNativeQuery("ALTER TABLE tbl_posts ALTER COLUMN post_id RESTART WITH 1").executeUpdate();
        em.createNativeQuery("ALTER TABLE tbl_post_attachments ALTER COLUMN attachment_id RESTART WITH 1").executeUpdate();
    }

    @BeforeEach
    public void setUp() {
        autoIncrementReset();
        em.clear();
    }

    private byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    @Test
    @DisplayName("Success : 게시글 저장")
    void savePost_Success1() {
        // Given
        MultipartFile[] files = {
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "file1-content".getBytes()),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "file2-content".getBytes())
        };

        PostReqDto.SavePostDto savePostDto = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L, 1L);

        // When
        Post savedPost = postService.savePost(1L, savePostDto, files);

        // Then
        assertNotNull(savedPost.getId());
        assertEquals("새로운 게시글", savedPost.getTitle());
        assertEquals("새로운 내용", savedPost.getContent());

        List<PostAttachment> attachments = postAttachmentRepository.findByPostId(savedPost.getId());
        assertEquals(2, attachments.size());
    }

    @Test
    @DisplayName("Success : 게시글 저장 - 첨부파일 x")
    void savePost_Success2() {
        // Given
        PostReqDto.SavePostDto savePostDto = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L, 1L);

        // When
        Post savedPost = postService.savePost(1L, savePostDto, null);

        // Then
        assertNotNull(savedPost.getId());
        assertEquals("새로운 게시글", savedPost.getTitle());
        assertEquals("새로운 내용", savedPost.getContent());

        List<PostAttachment> attachments = postAttachmentRepository.findByPostId(savedPost.getId());
        assertEquals(0, attachments.size());
    }

    @Test
    @DisplayName("Success : 게시글 수정 - 파일 추가")
    void updatePost_Success1() {
        // Given
        MultipartFile[] files = {
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "file1-content".getBytes()),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "file2-content".getBytes())
        };

        PostReqDto.SavePostDto savePostDto = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L, 1L);

        postService.savePost(1L, savePostDto, files);

        // When
        MultipartFile[] newFiles = {
                new MockMultipartFile("file3", "test3.jpg", "image/jpeg", "file3-content".getBytes())
        };

        PostReqDto.ModifyPostDto modifyPostDto =
                new PostReqDto.ModifyPostDto(1L, "수정된 제목", "수정된 내용", PostStatus.PRIVATE, 0L, null, null);

        Post updatedPost = postService.updatePost(1L, 1L, modifyPostDto, newFiles);

        // Then
        assertEquals("수정된 제목", updatedPost.getTitle());
        assertEquals("수정된 내용", updatedPost.getContent());

        List<PostAttachment> attachments = postAttachmentRepository.findByPostId(updatedPost.getId());
        assertEquals(3, attachments.size());
    }

    @Test
    @DisplayName("Success : 게시글 수정")
    void updatePost_Success2() {
        // Given
        MultipartFile[] files = {
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "file1-content".getBytes()),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "file2-content".getBytes())
        };

        PostReqDto.SavePostDto savePostDto = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L, 1L);

        postService.savePost(1L, savePostDto, files);

        // When
        MultipartFile[] newFiles = {
                new MockMultipartFile("file3", "test3.jpg", "image/jpeg", "file3-content".getBytes())
        };

        List<Long> removeList = new ArrayList<>();
        removeList.add(1L);

        PostReqDto.ModifyPostDto modifyPostDto =
                new PostReqDto.ModifyPostDto(1L, "수정된 제목", "수정된 내용", PostStatus.PRIVATE, 0L, null, removeList);

        Post updatedPost = postService.updatePost(1L, 1L, modifyPostDto, newFiles);

        // Then
        assertEquals("수정된 제목", updatedPost.getTitle());
        assertEquals("수정된 내용", updatedPost.getContent());

        List<PostAttachment> attachments = postAttachmentRepository.findByPostId(updatedPost.getId());
        assertEquals(2, attachments.size());
    }

    @Test
    @DisplayName("Fail : 게시글 수정 - 잘못된 post id")
    void updatePost_Fail() {
        // Given
        MultipartFile[] files = {
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "file1-content".getBytes()),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "file2-content".getBytes())
        };

        PostReqDto.SavePostDto savePostDto = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L, 1L);

        postService.savePost(1L, savePostDto, files);

        // When
        MultipartFile[] newFiles = {
                new MockMultipartFile("file3", "test3.jpg", "image/jpeg", "file3-content".getBytes())
        };

        List<Long> removeList = new ArrayList<>();
        removeList.add(2L);

        PostReqDto.ModifyPostDto modifyPostDto =
                new PostReqDto.ModifyPostDto(1L, "수정된 제목", "수정된 내용", PostStatus.PRIVATE, 0L, null, removeList);

        // Then
        assertThatThrownBy(() -> postService.updatePost(1L, 3L, modifyPostDto, newFiles))
                .isInstanceOf(PostException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", PostErrorCode.POST_NOT_FOUND)
                .hasMessage(PostErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Fail : 게시글 수정 - 첨부파일 용량 초과")
    void updatePost_Fail2() {
        // Given
        MultipartFile[] files = {
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", generateRandomBytes(2 * 1024 * 1024)),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", generateRandomBytes(2 * 1024 * 1024))
        };

        PostReqDto.SavePostDto savePostDto = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L, 1L);

        postService.savePost(1L, savePostDto, files);

        // When
        MultipartFile[] newFiles = {
                new MockMultipartFile("file3", "test3.jpg", "image/jpeg", generateRandomBytes(7 * 1024 * 1024))
        };

        PostReqDto.ModifyPostDto modifyPostDto =
                new PostReqDto.ModifyPostDto(1L, "수정된 제목", "수정된 내용", PostStatus.PRIVATE, 4L * 1024 * 1024, null, null);

        // Then
        assertThatThrownBy(() -> postService.updatePost(1L, 1L, modifyPostDto, newFiles))
                .isInstanceOf(FileException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", FileErrorCode.FILE_SIZE_EXCEEDED)
                .hasMessage(FileErrorCode.FILE_SIZE_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("Success : 게시물 삭제")
    public void deletePost_Success() {
        MultipartFile[] files = {
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", generateRandomBytes(2 * 1024 * 1024)),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", generateRandomBytes(2 * 1024 * 1024))
        };

        PostReqDto.SavePostDto savePostDto = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L, 1L);

        postService.savePost(1L, savePostDto, files);

        // When
        postService.deletePost(1L, 1L);

        // Then
        assertThatThrownBy(() -> postService.deletePost(1L, 1L))
                .isInstanceOf(PostException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", PostErrorCode.POST_NOT_FOUND)
                .hasMessage(PostErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Success : 게시글 불러오기")
    void getPost_success1() {
        // Given
        MultipartFile[] files = {
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "file1-content".getBytes()),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "file2-content".getBytes())
        };

        PostReqDto.SavePostDto savePostDto = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L, 1L);

        Post savedPost = postService.savePost(1L, savePostDto, files);
        Member savedMember = memberRepository.save(Member.builder().username("test").nickname("test").build());

        // When
        PostRespDto.GetPostDto respDto = postService.getPost(1L,1L);


        // Then
        assertEquals(savedPost.getId(), respDto.getPostId());
        assertEquals(savedPost.getTitle(), respDto.getTitle());
        assertEquals(savedPost.getContent(), respDto.getContent());
        assertEquals(savedPost.getPostStatus(), respDto.getPostStatus());
        assertEquals(savedPost.getGroupId(), respDto.getGroupId());
        assertEquals(2, respDto.getAttachments().size());
    }

    @Test
    @DisplayName("Fail : 삭제된 게시글 불러오기")
    void getPost_Fail() {
        // Given
        MultipartFile[] files = {
                new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "file1-content".getBytes()),
                new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "file2-content".getBytes())
        };

        PostReqDto.SavePostDto savePostDto = new PostReqDto.SavePostDto("새로운 게시글", "새로운 내용", PostStatus.PUBLIC, 1L, 1L);

        Post savedPost = postService.savePost(1L, savePostDto, files);
        Member savedMember = memberRepository.save(Member.builder().username("test").nickname("test").build());

        // When
        postService.deletePost(1L,1L);

        // Then
        assertThatThrownBy(() -> postService.getPost(1L, 1L))
                .isInstanceOf(PostException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", PostErrorCode.POST_NOT_FOUND)
                .hasMessage(PostErrorCode.POST_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Success : 게시글 목록 불러오기 [페이징] - 정렬 조건 x")
    public void getPosts_Success1() {
        for (int i = 15; i >= 1; i--) {
            Post post = Post.builder()
                    .title( i + " 테스트 제목")
                    .content( i + " 테스트 내용")
                    .postStatus(PostStatus.PUBLIC)
                    .groupId(1L)
                    .memberId(1L)
                    .build();
            postRepository.save(post);
        }
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.unsorted());

        Page<Post> result = postRepository.findAllBySearchStatus(1L, "", PostStatus.All, false, pageable);

        // Then
        assertEquals(15, result.getTotalElements()); // 전체 데이터 개수 확인
        assertEquals(10, result.getContent().size()); // 첫 페이지 10개
        assertEquals("1 테스트 제목", result.getContent().get(0).getTitle()); // 제목 확인
    }

    @Test
    @DisplayName("Success : 게시글 목록 불러오기 [페이징] - 정렬 조건 O")
    public void getPosts_Success2() {
        for (int i = 9; i >= 1; i--) {
            Post post = Post.builder()
                    .title( i + " 테스트 제목")
                    .content( i + " 테스트 내용")
                    .postStatus(PostStatus.PUBLIC)
                    .groupId(1L)
                    .memberId(1L)
                    .build();
            postRepository.save(post);
        }
        em.flush();
        em.clear();

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "title"));

        Page<Post> result = postRepository.findAllBySearchStatus(1L, "", PostStatus.All, false, pageable);

        // Then
        assertEquals(9, result.getTotalElements()); // 전체 데이터 개수 확인
        assertEquals(9, result.getContent().size()); // 첫 페이지 10개
        assertEquals("9 테스트 제목", result.getContent().get(0).getTitle()); // 제목 확인
    }

    @Test
    @DisplayName("Fail : 게시글 목록 불러오기 [페이징]")
    public void getPosts_Fail1() {
        for (int i = 15; i >= 1; i--) {
            Post post = Post.builder()
                    .title( i + " 테스트 제목")
                    .content( i + " 테스트 내용")
                    .postStatus(PostStatus.PUBLIC)
                    .groupId(1L)
                    .memberId(1L)
                    .build();
            postRepository.save(post);
        }
        em.flush();
        em.clear();

        List<Post> allPosts = postRepository.findAll();
        for (Post post : allPosts) {
            System.out.println(post.getTitle());  // 저장된 순서 확인
        }

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "fail"));

        // Then
        assertThatThrownBy(() -> postRepository.findAllBySearchStatus(1L, "", PostStatus.All, false, pageable))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("domainErrorCode", GlobalErrorCode.INVALID_INPUT_VALUE)
                .hasMessage(GlobalErrorCode.INVALID_INPUT_VALUE.getMessage());
    }

    @AfterAll
    public static void tearDown() {
        deleteTestUploadsFile();
    }

    private static void deleteTestUploadsFile(){
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