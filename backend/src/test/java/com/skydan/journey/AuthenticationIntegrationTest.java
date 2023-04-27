package com.skydan.journey;

import com.skydan.auth.AuthenticationRequest;
import com.skydan.auth.AuthenticationResponse;
import com.skydan.customer.CustomerDTO;
import com.skydan.customer.CustomerRegistrationRequest;
import com.skydan.jwt.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AuthenticationIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private JWTUtil jwtUtil;

    private static final Random RANDOM = new Random();
    private static final String AUTHENTICATION_PATH = "/api/v1/auth";
    private static final String CUSTOMER_PATH = "/api/v1/customers";

    @Test
    void canLogin() {

            //Create a registration customerRegistrationRequest
            String fullName = "Foo";
            String email = "example" + UUID.randomUUID() + "@skydan.com";
            int age = RANDOM.nextInt(18, 55);
            String gender = "MALE";

        String password = "password";
        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(
                    fullName, email, password, age, gender
            );

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(email, password);

        webTestClient.post()
                    .uri(AUTHENTICATION_PATH + "/login")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized();

            //Send a post customerRegistrationRequest
            webTestClient
                    .post()
                    .uri(CUSTOMER_PATH)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .body(Mono.just(customerRegistrationRequest), CustomerRegistrationRequest.class)
                    .exchange()
                    .expectStatus()
                    .isOk();

        EntityExchangeResult<AuthenticationResponse> result = webTestClient.post()
                .uri(AUTHENTICATION_PATH + "/login")
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(authenticationRequest), AuthenticationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<AuthenticationResponse>() {
                })
                .returnResult();

        String jwtToken = result.getResponseHeaders().get(AUTHORIZATION).get(0);
        AuthenticationResponse authenticationResponse = result.getResponseBody();

        CustomerDTO customerDTO = authenticationResponse.customerDTO();
        assertThat(jwtUtil.isTokenValid(jwtToken, customerDTO.username())).isTrue();
        assertThat(customerDTO.email()).isEqualTo(email);
        assertThat(customerDTO.age()).isEqualTo(age);
        assertThat(customerDTO.name()).isEqualTo(fullName);
        assertThat(customerDTO.username()).isEqualTo(email);
        assertThat(customerDTO.gender()).isEqualTo(gender);
        assertThat(customerDTO.roles()).isEqualTo(List.of("ROLE_USER"));

    }
}
