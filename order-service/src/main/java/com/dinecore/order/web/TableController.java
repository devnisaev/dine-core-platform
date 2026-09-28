package com.dinecore.order.web;

import com.dinecore.order.api.TableResponse;
import com.dinecore.order.service.TableService;
import com.dinecore.order.service.TenantIds;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
public class TableController {

    private final TableService tables;

    public TableController(TableService tables) {
        this.tables = tables;
    }

    @PostMapping
    public TableResponse create(@RequestBody CreateTableRequest request) {
        return tables.create(TenantIds.current(), request.tableNumber(), request.seats());
    }

    @GetMapping
    public List<TableResponse> list() {
        return tables.list(TenantIds.current());
    }

    public record CreateTableRequest(int tableNumber, int seats) {
    }
}
