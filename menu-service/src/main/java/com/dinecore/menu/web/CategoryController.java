package com.dinecore.menu.web;

import com.dinecore.menu.api.CategoryResponse;
import com.dinecore.menu.security.CurrentUser;
import com.dinecore.menu.service.CatalogService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CatalogService catalog;

    public CategoryController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @PostMapping
    public CategoryResponse create(@RequestBody CreateCategoryRequest request) {
        return catalog.createCategory(CurrentUser.organizationId(), request.name(), request.sortOrder());
    }

    @PatchMapping("/{id}")
    public CategoryResponse patch(@PathVariable UUID id, @RequestBody PatchCategoryRequest request) {
        return catalog.patchCategory(CurrentUser.organizationId(), id, request.name(), request.sortOrder(), request.active());
    }

    public record CreateCategoryRequest(String name, Integer sortOrder) {
    }

    public record PatchCategoryRequest(String name, Integer sortOrder, Boolean active) {
    }
}
