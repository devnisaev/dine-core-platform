package com.dinecore.order.web;

import com.dinecore.order.api.OrderResponse;
import com.dinecore.order.security.CurrentUser;
import com.dinecore.order.service.CancelService;
import com.dinecore.order.service.KitchenService;
import com.dinecore.order.service.OrderService;
import com.dinecore.order.service.ServeService;
import com.dinecore.order.service.SubmitService;
import com.dinecore.order.service.TenantIds;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orders;
    private final SubmitService submit;
    private final KitchenService kitchen;
    private final ServeService serve;
    private final CancelService cancel;

    public OrderController(OrderService orders, SubmitService submit, KitchenService kitchen, ServeService serve,
                           CancelService cancel) {
        this.orders = orders;
        this.submit = submit;
        this.kitchen = kitchen;
        this.serve = serve;
        this.cancel = cancel;
    }

    @PostMapping
    public OrderResponse open(@RequestBody OpenOrderRequest request) {
        return orders.open(CurrentUser.get(), TenantIds.current(), request.tableId(), request.note());
    }

    @PostMapping("/{id}/items")
    public OrderResponse addItem(@PathVariable UUID id, @RequestBody AddItemRequest request) {
        return orders.addItem(CurrentUser.get(), TenantIds.current(), id, request.dishId(), request.quantity(), request.modifierIds());
    }

    @PostMapping("/{id}/submit")
    public OrderResponse submit(@PathVariable UUID id, HttpServletRequest request) {
        return submit.submit(CurrentUser.get(), TenantIds.current(), id, request.getHeader("Authorization"));
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable UUID id, @RequestBody(required = false) CancelOrderRequest request) {
        String reason = request == null ? null : request.reason();
        return cancel.cancel(CurrentUser.get(), TenantIds.current(), id, reason);
    }

    @PostMapping("/{id}/items/{itemId}/ready")
    public OrderResponse ready(@PathVariable UUID id, @PathVariable UUID itemId) {
        return kitchen.ready(CurrentUser.get(), TenantIds.current(), id, itemId);
    }

    @PostMapping("/{id}/served")
    public OrderResponse served(@PathVariable UUID id) {
        return serve.served(CurrentUser.get(), TenantIds.current(), id);
    }

    @GetMapping
    public List<OrderResponse> list(@RequestParam(required = false) String status) {
        return orders.list(CurrentUser.get(), TenantIds.current(), status);
    }

    public record OpenOrderRequest(UUID tableId, String note) {
    }

    public record AddItemRequest(UUID dishId, int quantity, List<UUID> modifierIds) {
    }

    public record CancelOrderRequest(String reason) {
    }
}
