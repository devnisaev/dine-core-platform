package com.dinecore.tenant.repo;

import com.dinecore.tenant.domain.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    Optional<Shift> findByUserIdAndClosedAtIsNull(UUID userId);
}
