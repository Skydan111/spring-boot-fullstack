package com.skydan.journey;

import com.skydan.customer.CustomerDTO;
import com.skydan.customer.CustomerRegistrationRequest;
import com.skydan.customer.CustomerUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
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
public class CustomerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    private static final Random RANDOM = new Random();
    private static final String CUSTOMER_PATH = "/api/v1/customers";

    @Test
    void canRegisterCustomer() {
        //Create a registration request
        String fullName = "Foo";
        String email = "example" + UUID.randomUUID() + "@skydan.com";
        int age = RANDOM.nextInt(18, 55);
        String gender = "MALE";

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                fullName, email, "password", age, gender
        );

        //Send a post request
        String jwtToken = webTestClient
                .post()
                .uri(CUSTOMER_PATH)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Void.class)
                .getResponseHeaders()
                .get(AUTHORIZATION)
                .get(0);

        //Get all customers
        List<CustomerDTO> allCustomers = webTestClient
                .get()
                .uri(CUSTOMER_PATH)
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<CustomerDTO>() {
                })
                .returnResult()
                .getResponseBody();


        int id = allCustomers.stream()
                .filter(customer -> customer.email().equals(email))
                .map(CustomerDTO::id)
                .findFirst()
                .orElseThrow();

        //Make sure, that customers is present
        CustomerDTO expected = new CustomerDTO(
                id,
                fullName,
                email,
                age,
                gender,
                List.of("ROLE_USER"),
                email
        );

        assertThat(allCustomers).contains(expected);


        //Get customer by id
        webTestClient
                .get()
                .uri(CUSTOMER_PATH + "/{id}", id)
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<CustomerDTO>() {
                })
                .isEqualTo(expected);
    }

    @Test
    void canDeleteCustomer() {
        //Create a registration request
        String fullName = "Foo";
        String email = "example" + UUID.randomUUID() + "@skydan.com";
        int age = RANDOM.nextInt(18, 55);
        String gender = "MALE";

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                fullName, email, "password", age, gender
        );

        CustomerRegistrationRequest request2 = new CustomerRegistrationRequest(
                fullName, email + ".ua", "password", age, gender
        );

        //Send a post request to create first customer
        webTestClient
                .post()
                .uri(CUSTOMER_PATH)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();


        //Send a post request to create second customer
        String jwtToken = webTestClient
                .post()
                .uri(CUSTOMER_PATH)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(request2), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Void.class)
                .getResponseHeaders()
                .get(AUTHORIZATION)
                .get(0);

        //Get all customers
        List<CustomerDTO> allCustomers = webTestClient
                .get()
                .uri(CUSTOMER_PATH)
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<CustomerDTO>() {
                })
                .returnResult()
                .getResponseBody();

        int id = allCustomers.stream()
                .filter(customer -> customer.email().equals(email))
                .map(CustomerDTO::id)
                .findFirst()
                .orElseThrow();

        // second customer delete first customer
        webTestClient
                .delete()
                .uri(CUSTOMER_PATH + "/{id}", id)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();


        //second customer gets first customer by id
        webTestClient
                .get()
                .uri(CUSTOMER_PATH + "/{id}", id)
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void canUpdateCustomer() {

        String fullName = "Foo";
        String email = "example" + UUID.randomUUID() + "@skydan.com";
        int age = RANDOM.nextInt(18, 55);
        String gender = "MALE";

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                fullName, email, "password", age, gender
        );

        //Send a post request
        String jwtToken = webTestClient
                .post()
                .uri(CUSTOMER_PATH)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(Void.class)
                .getResponseHeaders()
                .get(AUTHORIZATION)
                .get(0);

        //Get all customers
        List<CustomerDTO> allCustomers = webTestClient
                .get()
                .uri(CUSTOMER_PATH)
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<CustomerDTO>() {
                })
                .returnResult()
                .getResponseBody();

        int id = allCustomers.stream()
                .filter(customer -> customer.email().equals(email))
                .map(CustomerDTO::id)
                .findFirst()
                .orElseThrow();

        // update customer

        String newName = "Maria";
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                newName, null, null, null
        );

        webTestClient
                .put()
                .uri(CUSTOMER_PATH + "/{id}", id)
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(APPLICATION_JSON)
                .body(Mono.just(updateRequest), CustomerUpdateRequest.class)
                .exchange()
                .expectStatus()
                .isOk();


        //Get customer by id
        CustomerDTO updatedCustomer = webTestClient
                .get()
                .uri(CUSTOMER_PATH + "/{id}", id)
                .accept(APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(CustomerDTO.class)
                .returnResult()
                .getResponseBody();

        CustomerDTO expected = new CustomerDTO(id, newName, email, age, gender, List.of("ROLE_USER"), email);

        assertThat(updatedCustomer).isEqualTo(expected);
    }
}
