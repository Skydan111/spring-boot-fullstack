package com.skydan.customer;

public record CustomerUpdateRequest(
        String name,
        String email,
        Integer age,
        String gender
) {
}
