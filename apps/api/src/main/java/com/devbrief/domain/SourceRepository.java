package com.devbrief.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, Long> {
    Optional<Source> findByName(String name);

    List<Source> findByEnabledTrueOrderByNameAsc();
}

