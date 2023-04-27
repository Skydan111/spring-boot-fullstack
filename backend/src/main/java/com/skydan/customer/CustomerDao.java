package com.skydan.customer;

import java.util.List;
import java.util.Optional;

public interface CustomerDao {
    List<Customer> selectAllCustomers();
    Optional<Customer> selectCustomerById(Integer customerId);
    void insertCustomer(Customer customer);
    boolean existsCustomerWithEmail(String email);
    void deleteCustomerById(Integer customerId);
    boolean existsCustomerWithId(Integer customerId);
    void updateCustomer(Customer update);
    Optional<Customer> selectUserByEmail(String email);
}
