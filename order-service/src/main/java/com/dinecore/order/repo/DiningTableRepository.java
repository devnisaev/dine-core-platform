package com.dinecore.order.repo;

import com.dinecore.order.domain.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiningTableRepository extends JpaRepository<DiningTable, UUID> {

    boolean existsByBranchIdAndTableNumber(UUID branchId, int tableNumber);

    List<DiningTable> findByBranchIdOrderByTableNumberAsc(UUID branchId);
}
