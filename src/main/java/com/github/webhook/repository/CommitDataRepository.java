package com.github.webhook.repository;

import com.github.webhook.model.CommitData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitDataRepository extends JpaRepository<CommitData, Long> {
}
