package com.skydan.auth;

import com.skydan.customer.CustomerDTO;

public record AuthenticationResponse(
        String token,
        CustomerDTO customerDTO
) {
}
