package com.devbrief.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BriefingRepository extends JpaRepository<Briefing, Long> {
    List<Briefing> findTop5ByOrderByClusterScoreDescGeneratedAtDesc();
}

