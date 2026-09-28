package com.dinecore.menu.event;

import com.dinecore.menu.cache.MenuCache;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MenuChangedListener {

    private final MenuCache cache;

    public MenuChangedListener(MenuCache cache) {
        this.cache = cache;
    }

    @EventListener
    public void onMenuChanged(MenuChanged event) {
        if (event.branchId() == null) {
            cache.evictOrganization(event.organizationId());
            return;
        }
        cache.evictBranch(event.branchId());
    }
}
