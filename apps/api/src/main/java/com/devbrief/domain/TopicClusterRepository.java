package com.devbrief.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicClusterRepository extends JpaRepository<TopicCluster, Long> {
    List<TopicCluster> findTop20ByOrderByScoreDescLastSeenAtDesc();
}

