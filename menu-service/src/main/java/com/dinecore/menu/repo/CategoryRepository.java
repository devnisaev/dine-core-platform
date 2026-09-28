package com.dinecore.menu.repo;

import com.dinecore.menu.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByOrganizationIdAndActiveTrueOrderBySortOrderAsc(UUID organizationId);
}
