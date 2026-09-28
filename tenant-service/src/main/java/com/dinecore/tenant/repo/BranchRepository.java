package com.dinecore.tenant.repo;

import com.dinecore.tenant.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {

    boolean existsByCode(String code);

    List<Branch> findByOrganizationId(UUID organizationId);
}
