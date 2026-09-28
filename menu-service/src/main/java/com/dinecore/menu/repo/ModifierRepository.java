package com.dinecore.menu.repo;

import com.dinecore.menu.domain.Modifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModifierRepository extends JpaRepository<Modifier, UUID> {

    List<Modifier> findByDishIdOrderByNameAsc(UUID dishId);
}
