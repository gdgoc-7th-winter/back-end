package com.project.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.user.domain.entity.Department;
import com.project.user.domain.entity.TechStack;
import com.project.user.domain.entity.Track;
import com.project.user.domain.repository.DepartmentRepository;
import com.project.user.domain.repository.TechStackRepository;
import com.project.user.domain.repository.TrackRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TrackRepository trackRepository;
    private final TechStackRepository techStackRepository;
    private final DepartmentRepository departmentRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeTracksAndTechStacks();
        initializeDepartments();
    }

    private void initializeTracksAndTechStacks() {
        if (trackRepository.count() > 0 || techStackRepository.count() > 0) {
            log.info("트랙/기술스택 초기 데이터가 이미 존재하여 건너뜁니다.");
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource("tracks-and-techStacks.json");
            TracksAndTechStacksDto data = objectMapper.readValue(resource.getInputStream(), TracksAndTechStacksDto.class);

            List<Track> tracks = data.getTracks().stream()
                    .map(name -> new Track(name))
                    .toList();
            trackRepository.saveAll(tracks);

            List<TechStack> techStacks = data.getTechStacks().stream()
                    .map(name -> new TechStack(name))
                    .toList();
            techStackRepository.saveAll(techStacks);

            log.info("트랙 {}개, 기술스택 {}개 초기화 완료", tracks.size(), techStacks.size());
        } catch (Exception e) {
            log.error("트랙/기술스택 초기화 중 오류 발생: {}", e.getMessage());
        }
    }

    private void initializeDepartments() {
        if (departmentRepository.count() > 0) {
            log.info("학과 초기 데이터가 이미 존재하여 건너뜁니다.");
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource("departments.json");
            DepartmentsDto data = objectMapper.readValue(resource.getInputStream(), DepartmentsDto.class);

            List<Department> departments = data.getDepartments().stream()
                    .map(entry -> Department.builder()
                            .college(entry.getCollege())
                            .name(entry.getName())
                            .build())
                    .toList();
            departmentRepository.saveAll(departments);

            log.info("학과 {}개 초기화 완료", departments.size());
        } catch (Exception e) {
            log.error("학과 초기화 중 오류 발생: {}", e.getMessage());
        }
    }
}
