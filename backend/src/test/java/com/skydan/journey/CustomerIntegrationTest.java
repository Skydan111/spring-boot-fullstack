package com.skydan.journey;

import com.skydan.customer.Customer;
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
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CustomerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;

    private static final Random RANDOM = new Random();
    private static final String CUSTOMER_URI = "/api/v1/customers";

    @Test
    void canRegisterCustomer() {
        //Create a registration request
        String fullName = "Foo";
        String email = "example" + UUID.randomUUID() + "@skydan.com";
        int age = RANDOM.nextInt(18, 55);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                fullName, email, age
        );

        //Send a post request
        webTestClient
                .post()
                .uri(CUSTOMER_URI)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();

        //Get all customers
        List<Customer> allCustomers = webTestClient
                .get()
                .uri(CUSTOMER_URI)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Customer>() {
                })
                .returnResult()
                .getResponseBody();

        //Make sure, that customers is present
        Customer expected = new Customer(
                fullName, email, age
        );

        assertThat(allCustomers)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                .contains(expected);

        int id = allCustomers.stream()
                .filter(customer -> customer.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        expected.setId(id);


        //Get customer by id
        webTestClient
                .get()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(new ParameterizedTypeReference<Customer>() {
                })
                .isEqualTo(expected);
    }

    @Test
    void canDeleteCustomer() {
        //Create a registration request
        String fullName = "Foo";
        String email = "example" + UUID.randomUUID() + "@skydan.com";
        int age = RANDOM.nextInt(18, 55);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                fullName, email, age
        );

        //Send a post request
        webTestClient
                .post()
                .uri(CUSTOMER_URI)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();

        //Get all customers
        List<Customer> allCustomers = webTestClient
                .get()
                .uri(CUSTOMER_URI)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Customer>() {
                })
                .returnResult()
                .getResponseBody();

        int id = allCustomers.stream()
                .filter(customer -> customer.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        // delete customer
        webTestClient
                .delete()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk();


        //Get customer by id
        webTestClient
                .get()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    void canUpdateCustomer() {

        String fullName = "Foo";
        String email = "example" + UUID.randomUUID() + "@skydan.com";
        int age = RANDOM.nextInt(18, 55);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                fullName, email, age
        );

        //Send a post request
        webTestClient
                .post()
                .uri(CUSTOMER_URI)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(request), CustomerRegistrationRequest.class)
                .exchange()
                .expectStatus()
                .isOk();

        //Get all customers
        List<Customer> allCustomers = webTestClient
                .get()
                .uri(CUSTOMER_URI)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(new ParameterizedTypeReference<Customer>() {
                })
                .returnResult()
                .getResponseBody();

        int id = allCustomers.stream()
                .filter(customer -> customer.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        // update customer

        String newName = "Maria";
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                newName, null, null
        );

        webTestClient
                .put()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .body(Mono.just(updateRequest), CustomerUpdateRequest.class)
                .exchange()
                .expectStatus()
                .isOk();


        //Get customer by id
        Customer updatedCustomer = webTestClient
                .get()
                .uri(CUSTOMER_URI + "/{id}", id)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Customer.class)
                .returnResult()
                .getResponseBody();

        Customer expected = new Customer(id, newName, email, age);

        assertThat(updatedCustomer).isEqualTo(expected);
    }
}
