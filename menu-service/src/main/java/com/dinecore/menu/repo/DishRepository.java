package com.dinecore.menu.repo;

import com.dinecore.menu.domain.Dish;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID> {

    List<Dish> findByCategoryIdAndActiveTrueOrderByNameAsc(UUID categoryId);
}
