package com.project.post.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.post.domain.entity.Board;
import com.project.post.domain.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardInitializer implements CommandLineRunner {

    private final BoardRepository boardRepository;
    private final ObjectMapper objectMapper;

    private record BoardSeed(String code, String name) {}

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("boards.json");
        if (!resource.exists()) {
            throw new IllegalStateException("boards.json을 classpath에서 찾을 수 없습니다.");
        }

        try (InputStream is = resource.getInputStream()) {
            List<BoardSeed> seeds = objectMapper.readValue(is, new TypeReference<>() {});
            if (seeds == null || seeds.isEmpty()) {
                throw new IllegalStateException("boards.json이 비어 있거나 형식이 올바르지 않습니다.");
            }
            for (BoardSeed seed : seeds) {
                if (boardRepository.findByCode(seed.code()).isEmpty()) {
                    boardRepository.save(Board.of(seed.code(), seed.name()));
                    log.info("게시판 초기 행 추가: {} ({})", seed.code(), seed.name());
                }
            }
        }
    }
}
