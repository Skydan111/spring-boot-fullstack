package com.skydan.auth;

public record AuthenticationRequest(
        String userName,
        String password
) {
}
