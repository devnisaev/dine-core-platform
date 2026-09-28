package com.dinecore.menu.web;

import com.dinecore.menu.api.MenuView;
import com.dinecore.menu.security.CurrentUser;
import com.dinecore.menu.service.MenuQuery;
import com.dinecore.menu.service.TenantIds;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {

    private final MenuQuery menu;

    public MenuController(MenuQuery menu) {
        this.menu = menu;
    }

    @GetMapping
    public MenuView get() {
        return menu.read(CurrentUser.organizationId(), TenantIds.current());
    }
}
