package com.dinecore.order.repo;

import com.dinecore.order.domain.DiningOrder;
import com.dinecore.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DiningOrderRepository extends JpaRepository<DiningOrder, UUID> {

    List<DiningOrder> findByBranchId(UUID branchId);

    List<DiningOrder> findByBranchIdAndStatus(UUID branchId, OrderStatus status);
}
