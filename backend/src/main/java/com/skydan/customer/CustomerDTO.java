package com.skydan.customer;

import java.util.List;

public record CustomerDTO (
        Integer id,
        String name,
        String email,
        Integer age,
        String gender,
        List<String> roles,
        String username
) {
}
