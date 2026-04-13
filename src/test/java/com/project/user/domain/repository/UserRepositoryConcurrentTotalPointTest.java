package com.project.user.domain.repository;

import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryConcurrentTotalPointTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LevelBadgeRepository levelBadgeRepository;

    @Test
    @DisplayName("동시 addTotalPoints 호출 시 총점 유실이 없어야 한다")
    void concurrentAtomicAdds() throws Exception {
        LevelBadge badge = levelBadgeRepository.findByPointWithinRange(0).orElseThrow();
        User u = User.builder()
                .email("conc-" + UUID.randomUUID() + "@test.com")
                .password("pw")
                .nickname("c-" + UUID.randomUUID().toString().substring(0, 24))
                .build();
        u.initializeLevelBadge(badge);
        Long userId = userRepository.saveAndFlush(u).getId();

        int threads = 20;
        int deltaEach = 3;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                int updated = userRepository.addTotalPoints(userId, deltaEach);
                if (updated != 1) {
                    throw new IllegalStateException("update not applied");
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        try {
            User reloaded = userRepository.findById(userId).orElseThrow();
            assertThat(reloaded.getTotalPoint()).isEqualTo(threads * deltaEach);
        } finally {
            userRepository.deleteById(userId);
        }
    }
}
