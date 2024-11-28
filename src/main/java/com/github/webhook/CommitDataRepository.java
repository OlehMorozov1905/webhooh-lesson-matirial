package com.github.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitDataRepository extends JpaRepository<CommitData, Long> {
}
