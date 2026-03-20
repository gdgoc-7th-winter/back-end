package com.project.user.domain.repository;

import com.project.user.domain.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackRepository extends JpaRepository<Track, Long> {
    List<Track> findByNameIn(List<String> names);
}
