package com.dinecore.menu.cache;

import com.dinecore.menu.api.MenuView;

import java.util.Optional;
import java.util.UUID;

public interface MenuCache {

    Optional<MenuView> get(UUID branchId);

    void put(UUID organizationId, UUID branchId, MenuView view);

    void evictBranch(UUID branchId);

    void evictOrganization(UUID organizationId);
}
