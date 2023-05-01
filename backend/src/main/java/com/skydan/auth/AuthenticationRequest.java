package com.skydan.auth;

public record AuthenticationRequest(
        String username,
        String password
) {
}
