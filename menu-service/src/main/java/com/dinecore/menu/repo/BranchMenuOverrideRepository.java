package com.dinecore.menu.repo;

import com.dinecore.menu.domain.BranchMenuOverride;
import com.dinecore.menu.domain.OverrideKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchMenuOverrideRepository extends JpaRepository<BranchMenuOverride, OverrideKey> {
}
