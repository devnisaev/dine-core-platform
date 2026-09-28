package com.dinecore.menu.cache;

import com.dinecore.menu.api.MenuView;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryMenuCache implements MenuCache {

    private final Map<UUID, MenuView> menus = new ConcurrentHashMap<>();
    private final Map<UUID, Set<UUID>> branches = new ConcurrentHashMap<>();

    @Override
    public Optional<MenuView> get(UUID branchId) {
        return Optional.ofNullable(menus.get(branchId));
    }

    @Override
    public void put(UUID organizationId, UUID branchId, MenuView view) {
        menus.put(branchId, view);
        branches.computeIfAbsent(organizationId, id -> ConcurrentHashMap.newKeySet()).add(branchId);
    }

    @Override
    public void evictBranch(UUID branchId) {
        menus.remove(branchId);
    }

    @Override
    public void evictOrganization(UUID organizationId) {
        branches.getOrDefault(organizationId, Set.of()).forEach(menus::remove);
    }
}
