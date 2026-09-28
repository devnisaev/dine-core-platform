package com.dinecore.menu.event;

import com.dinecore.menu.cache.MenuCache;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MenuNotifier {

    private final MenuCache cache;
    private final ApplicationEventPublisher events;

    public MenuNotifier(MenuCache cache, ApplicationEventPublisher events) {
        this.cache = cache;
        this.events = events;
    }

    public void branchChanged(UUID organizationId, UUID branchId) {
        cache.evictBranch(branchId);
        events.publishEvent(MenuChanged.branch(organizationId, branchId));
    }

    public void catalogChanged(UUID organizationId) {
        cache.evictOrganization(organizationId);
        events.publishEvent(MenuChanged.catalog(organizationId));
    }
}
