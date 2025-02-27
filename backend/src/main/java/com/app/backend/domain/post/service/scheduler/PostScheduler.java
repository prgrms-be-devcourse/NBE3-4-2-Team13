package com.app.backend.domain.post.service.scheduler;

import com.app.backend.domain.post.entity.Post;
import com.app.backend.domain.post.repository.post.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostRepository postRepository;
    private static final String POST_UPDATE = "post:update";
    private static final String POST_HISTORY = "post:history";
    private static final String VIEW_COUNT_PREFIX = "viewCount:post:postid:";

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

    private void processViewCountSave(String typeKey, boolean isReset) {
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
}
