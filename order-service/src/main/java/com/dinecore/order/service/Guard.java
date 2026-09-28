package com.dinecore.order.service;

import com.dinecore.common.Role;
import com.dinecore.order.domain.DiningOrder;
import com.dinecore.order.domain.OrderStatus;
import com.dinecore.order.error.ApiException;
import org.springframework.http.HttpStatus;

public final class Guard {

    private Guard() {
    }

    public static void waiterOwns(Caller caller, DiningOrder order) {
        if (caller.role() != Role.WAITER || !caller.userId().equals(order.getWaiterId())) {
            throw forbidden();
        }
    }

    public static void draftCancel(Caller caller, DiningOrder order) {
        if (caller.role() == Role.BRANCH_ADMIN) {
            return;
        }
        waiterOwns(caller, order);
    }

    public static void kitchenCancel(Caller caller) {
        if (caller.role() != Role.BRANCH_ADMIN) {
            throw forbidden();
        }
    }

    public static void draft(DiningOrder order) {
        if (order.getStatus() != OrderStatus.DRAFT) {
            throw state("Items can be added only while the order is a draft");
        }
    }

    public static void available(com.dinecore.order.domain.TableStatus status) {
        if (status != com.dinecore.order.domain.TableStatus.AVAILABLE) {
            throw state("Table is not available");
        }
    }

    public static ApiException state(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "ORDER_STATE", message);
    }

    public static ApiException forbidden() {
        return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Not allowed");
    }

    public static ApiException missing(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
    }
}
