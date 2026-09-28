package com.dinecore.menu;

import com.dinecore.menu.api.MenuView;
import com.dinecore.menu.cache.MenuCache;
import com.dinecore.menu.event.MenuChanged;
import com.dinecore.menu.service.CatalogService;
import com.dinecore.menu.service.MenuQuery;
import com.dinecore.menu.service.OverrideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@RecordApplicationEvents
class MenuIsolationTest {

    @Autowired
    private CatalogService catalog;

    @Autowired
    private OverrideService overrides;

    @Autowired
    private MenuQuery menu;

    @Autowired
    private MenuCache cache;

    @Autowired
    private ApplicationEvents events;

    @Test
    void overrideChangesOnlyThatBranch() {
        UUID organizationId = UUID.randomUUID();
        UUID branchA = UUID.randomUUID();
        UUID branchB = UUID.randomUUID();
        UUID categoryId = catalog.createCategory(organizationId, "Hot", 1).id();
        UUID dishId = catalog.createDish(organizationId, categoryId, "Lagman", "soup", new BigDecimal("450.00")).id();
        catalog.createModifier(organizationId, dishId, "extra meat", new BigDecimal("50.00"));

        menu.read(organizationId, branchA);
        menu.read(organizationId, branchB);
        overrides.put(organizationId, branchA, dishId, new BigDecimal("480.00"), true);

        assertThat(cache.get(branchA)).isEmpty();
        assertThat(price(menu.read(organizationId, branchA), dishId)).isEqualByComparingTo("480.00");
        assertThat(price(menu.read(organizationId, branchB), dishId)).isEqualByComparingTo("450.00");
        assertThat(events.stream(MenuChanged.class).anyMatch(event -> branchA.equals(event.branchId()))).isTrue();
    }

    @Test
    void catalogChangeRefreshesEveryCachedBranch() {
        UUID organizationId = UUID.randomUUID();
        UUID branchA = UUID.randomUUID();
        UUID branchB = UUID.randomUUID();
        UUID categoryId = catalog.createCategory(organizationId, "Hot", 1).id();
        UUID dishId = catalog.createDish(organizationId, categoryId, "Lagman", null, new BigDecimal("450.00")).id();
        menu.read(organizationId, branchA);
        menu.read(organizationId, branchB);

        catalog.patchDish(organizationId, dishId, null, null, null, new BigDecimal("470.00"), null);

        assertThat(price(menu.read(organizationId, branchA), dishId)).isEqualByComparingTo("470.00");
        assertThat(price(menu.read(organizationId, branchB), dishId)).isEqualByComparingTo("470.00");
        assertThat(events.stream(MenuChanged.class).anyMatch(event -> event.branchId() == null)).isTrue();
    }

    @Test
    void unavailableDishIsHiddenFromThatBranchOnly() {
        UUID organizationId = UUID.randomUUID();
        UUID branchA = UUID.randomUUID();
        UUID branchB = UUID.randomUUID();
        UUID categoryId = catalog.createCategory(organizationId, "Hot", 1).id();
        UUID dishId = catalog.createDish(organizationId, categoryId, "Lagman", null, new BigDecimal("450.00")).id();

        overrides.put(organizationId, branchB, dishId, null, false);

        assertThat(price(menu.read(organizationId, branchA), dishId)).isEqualByComparingTo("450.00");
        assertThat(menu.read(organizationId, branchB).categories()).isEmpty();
    }

    private static BigDecimal price(MenuView view, UUID dishId) {
        return view.categories().stream()
                .flatMap(category -> category.dishes().stream())
                .filter(dish -> dishId.equals(dish.id()))
                .map(MenuView.MenuDish::price)
                .findFirst()
                .orElseThrow();
    }
}
