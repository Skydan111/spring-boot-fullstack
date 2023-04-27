package com.skydan.customer;

import com.skydan.exception.DuplicateResourceException;
import com.skydan.exception.RequestValidationException;
import com.skydan.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock private CustomerDao customerDao;
    private CustomerService underTest;
    @Mock private PasswordEncoder passwordEncoder;
    private final CustomerDTOMapper customerDTOMapper = new CustomerDTOMapper();

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerDao, customerDTOMapper, passwordEncoder);
    }

    @Test
    void getAllCustomers() {
        //When
        underTest.getAllCustomers();

        //Then
        verify(customerDao).selectAllCustomers();
    }

    @Test
    void canGetCustomer() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "Maria", "maria@gmail.com", "password", 18, "FEMALE");
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerDTO expected = customerDTOMapper.apply(customer);

        //When
        CustomerDTO actual = underTest.getCustomer(1);

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void willThrowWhenGetCustomerReturnEmptyOptional() {
        //Given
        int id = 1;

        when(customerDao.selectCustomerById(id)).thenReturn(Optional.empty());

        //When

        //Then
        assertThatThrownBy(() -> underTest.getCustomer(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));
    }

    @Test
    void addCustomer() {
        //Given
        String email = "maria@gmail.com";

        when(customerDao.existsCustomerWithEmail(email)).thenReturn(false);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Maria", email, "password", 18, "FEMALE"
        );

        String passwordHash = "54t4;v883dn;65yh";

        when(passwordEncoder.encode(request.password())).thenReturn(passwordHash);

        //When
        underTest.addCustomer(request);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);

        verify(customerDao).insertCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isNull();
        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(capturedCustomer.getPassword()).isEqualTo(passwordHash);
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
        assertThat(capturedCustomer.getGender()).isEqualTo(request.gender());
    }

    @Test
    void willThrowWhenEmailExistsWhileAddingACustomer() {
        //Given
        String email = "maria@gmail.com";

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Maria", email, "password", 18, "FEMALE"
        );
        when(customerDao.existsCustomerWithEmail(email)).thenReturn(true);

        //When
        assertThatThrownBy(() -> underTest.addCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already taken");

        //Then
        verify(customerDao, never()).insertCustomer(any());
    }

    @Test
    void deleteCustomerById() {
        //Given
        int id = 1;

        when(customerDao.existsCustomerWithId(id)).thenReturn(true);

        //When
        underTest.deleteCustomerById(id);

        //Then
        verify(customerDao).deleteCustomerById(id);
    }
    @Test
    void willThrowWhenDeleteCustomerByIdNotExist() {
        //Given
        int id = 1;

        when(customerDao.existsCustomerWithId(id)).thenReturn(false);

        //When
        assertThatThrownBy(() -> underTest.deleteCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));


        //Then
        verify(customerDao, never()).deleteCustomerById(id);
    }


    @Test
    void canUpdateAllCustomerProperties() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "Maria", "maria@gmail.com", "password", 18, "FEMALE");
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        String newEmail = "marianna@gmail.com";
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "Marianna", newEmail, 20, "MALE");

        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);

        //When
        underTest.updateCustomer(id, updateRequest);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(updateRequest.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(updateRequest.age());
        assertThat(capturedCustomer.getGender()).isEqualTo(updateRequest.gender());
    }

    @Test
    void canUpdateOnlyCustomerName() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "Maria", "maria@gmail.com", "password", 18, "FEMALE");
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                "Marianna", null, null, null);

        //When
        underTest.updateCustomer(id, updateRequest);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(capturedCustomer.getGender()).isEqualTo(customer.getGender());
    }

    @Test
    void canUpdateOnlyCustomerEmail() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "Maria", "maria@gmail.com", "password", 18, "FEMALE");
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        String newEmail = "marianna@gmail.com";
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                null, newEmail, null, null);

        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(false);

        //When
        underTest.updateCustomer(id, updateRequest);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(newEmail);
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(capturedCustomer.getGender()).isEqualTo(customer.getGender());
    }

    @Test
    void canUpdateOnlyCustomerAge() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "Maria", "maria@gmail.com", "password", 18, "FEMALE");
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                null, null, 20, null);

        //When
        underTest.updateCustomer(id, updateRequest);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getAge()).isEqualTo(updateRequest.age());
        assertThat(capturedCustomer.getGender()).isEqualTo(customer.getGender());
    }

    @Test
    void canUpdateOnlyCustomerGender() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "Maria", "maria@gmail.com", "password", 18, "FEMALE");
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                null, null, null, "MALE");

        //When
        underTest.updateCustomer(id, updateRequest);

        //Then
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(capturedCustomer.getGender()).isEqualTo(updateRequest.gender());
    }

    @Test
    void willThrowWhenTryingToUpdateCustomerEmailWhenAlreadyTaken() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "Maria", "maria@gmail.com", "password", 18, "FEMALE");
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        String newEmail = "marianna@gmail.com";
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                null, newEmail, null, null);

        when(customerDao.existsCustomerWithEmail(newEmail)).thenReturn(true);

        //When
        assertThatThrownBy(() -> underTest.updateCustomer(id, updateRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already taken");

        //Then
        verify(customerDao, never()).updateCustomer(any());
    }

    @Test
    void willThrowWhenCustomerUpdateHasNoChanges() {
        //Given
        int id = 1;
        Customer customer = new Customer(id, "Maria", "maria@gmail.com", "password", 18, "FEMALE");
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                customer.getName(), customer.getEmail(), customer.getAge(), customer.getGender());

        //When
        assertThatThrownBy(() -> underTest.updateCustomer(id, updateRequest))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("no data changes found");

        //Then
        verify(customerDao, never()).updateCustomer(any());
    }
}
