package com.dinecore.order.service;

import com.dinecore.order.api.TableResponse;
import com.dinecore.order.domain.DiningTable;
import com.dinecore.order.error.ApiException;
import com.dinecore.order.notify.BranchTopics;
import com.dinecore.order.notify.Destinations;
import com.dinecore.order.repo.DiningTableRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TableService {

    private final DiningTableRepository tables;
    private final BranchTopics topics;

    public TableService(DiningTableRepository tables, BranchTopics topics) {
        this.tables = tables;
        this.topics = topics;
    }

    @Transactional
    public TableResponse create(UUID branchId, int tableNumber, int seats) {
        Checks.positive(tableNumber, "Table number");
        Checks.positive(seats, "Seats");
        if (tables.existsByBranchIdAndTableNumber(branchId, tableNumber)) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE", "Table number already exists");
        }
        DiningTable table = tables.save(new DiningTable(UUID.randomUUID(), branchId, tableNumber, seats));
        TableResponse response = TableResponse.from(table);
        topics.send(Destinations.tables(branchId), response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<TableResponse> list(UUID branchId) {
        return tables.findByBranchIdOrderByTableNumberAsc(branchId).stream().map(TableResponse::from).toList();
    }
}
