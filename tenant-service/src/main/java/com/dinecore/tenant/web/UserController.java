package com.dinecore.tenant.web;

import com.dinecore.common.Role;
import com.dinecore.tenant.api.UserResponse;
import com.dinecore.tenant.error.ApiException;
import com.dinecore.tenant.security.CurrentUser;
import com.dinecore.tenant.service.CreateUser;
import com.dinecore.tenant.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @PostMapping
    public UserResponse create(@RequestBody CreateUserRequest request) {
        CreateUser command = new CreateUser(request.username(), request.password(), role(request.role()), request.organizationId());
        return users.create(command, CurrentUser.get());
    }

    @GetMapping
    public List<UserResponse> list() {
        return users.list(CurrentUser.get());
    }

    private static Role role(String value) {
        try {
            return Role.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION", "Unknown role");
        }
    }

    public record CreateUserRequest(String username, String password, String role, UUID organizationId) {
    }
}
