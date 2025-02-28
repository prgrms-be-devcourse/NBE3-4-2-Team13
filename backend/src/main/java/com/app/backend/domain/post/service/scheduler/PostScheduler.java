package com.app.backend.domain.post.service.scheduler;

import com.app.backend.domain.attachment.service.FileService;
import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.entity.PostAttachment;
import com.app.backend.domain.post.repository.post.PostRepository;
import com.app.backend.domain.post.repository.postAttachment.PostAttachmentRepository;
import com.app.backend.global.config.FileConfig;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostScheduler {

    private final RedisTemplate<String, Object> redisTemplate;

    private final FileConfig fileConfig;
    private final FileService fileService;
    private final PostRepository postRepository;
    private final PostAttachmentRepository postAttachmentRepository;

    private static final String POST_UPDATE = "post:update";
    private static final String POST_HISTORY = "post:history";
    private static final String VIEW_COUNT_PREFIX = "viewCount:post:postid:";
    private static final int deleteDays = 7;

    @Transactional
    @Scheduled(fixedRate = 600_000) // 10분
    public void viewCountsRedisToRDB() {
        processViewCountSave(POST_UPDATE, false);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshViewCount() {
        processViewCountSave(POST_HISTORY, true);
    }

    @Transactional
    @Scheduled(cron = "0 0 4 * * ?")
    public void deletePosts() {
        LocalDateTime deleteDay = LocalDate.now().minusDays(deleteDays).atStartOfDay();

        processDeletePosts(deleteDay);
        processDeleteFiles(deleteDay);
    }

    private void processViewCountSave(final String typeKey, final boolean isReset) {
        try {
            Set<Object> updatedPostIds = redisTemplate.opsForSet().members(typeKey);

            if (updatedPostIds == null || updatedPostIds.isEmpty()) {
                log.info("동기화 데이터가 존재하지 않습니다");
                return;
            }

            List<Long> postIds = updatedPostIds.stream()
                    .map(key -> key.toString().substring(key.toString().lastIndexOf(":") + 1))
                    .map(Long::parseLong)
                    .toList();

            List<Post> posts = postRepository.findAllById(postIds);

            posts.forEach(post -> {
                String viewCountKey = VIEW_COUNT_PREFIX + post.getId();
                Object viewCountValue = redisTemplate.opsForValue().get(viewCountKey);
                if (viewCountValue != null) {
                    post.addTodayViewCount(Long.parseLong(viewCountValue.toString()));
                    redisTemplate.delete(viewCountKey);
                }
                if (isReset) {
                    post.refreshViewCount();
                }
            });

            postRepository.saveAll(posts);
            redisTemplate.delete(POST_UPDATE);

            if (isReset) {
                redisTemplate.delete(POST_HISTORY);
            }

            log.info("데이터 동기화를 완료했습니다");
        } catch (Exception e) {
            log.error("데이터 동기화에 실패했습니다");
        }
    }

    private void processDeletePosts(final LocalDateTime deleteDay) {
        postRepository.deleteAllByModifiedAtAndDisabled(deleteDay, true);
    }

    private void processDeleteFiles(final LocalDateTime deleteDay) {
        List<PostAttachment> files = postAttachmentRepository.findAllByModifiedAtAndDisabled(deleteDay, true);

        if(files.isEmpty()){
            return;
        }

        List<String> deleteFilePaths = new ArrayList<>();
        List<Long> deleteFileIds = new ArrayList<>();

        files.forEach(file -> {
            deleteFileIds.add(file.getId());
            deleteFilePaths.add("%s/%s".formatted(fileConfig.getBASE_DIR(), file.getStoreFilePath()));
        });

        postAttachmentRepository.deleteByFileIdList(deleteFileIds);
        fileService.deleteFiles(deleteFilePaths);
    }
}
