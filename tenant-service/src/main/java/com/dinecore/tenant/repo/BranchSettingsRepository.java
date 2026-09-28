package com.dinecore.tenant.repo;

import com.dinecore.tenant.domain.BranchSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BranchSettingsRepository extends JpaRepository<BranchSettings, UUID> {
}
