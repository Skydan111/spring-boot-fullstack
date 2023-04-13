package com.skydan.customer;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


public class CustomerRawMapperTest {

    @Test
    void mapRow() throws SQLException {
        //Given
        CustomerRawMapper customerRawMapper = new CustomerRawMapper();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("Maria");
        when(resultSet.getString("email")).thenReturn("maria@gmail.com");
        when(resultSet.getInt("age")).thenReturn(19);
        when(resultSet.getString("gender")).thenReturn("FEMALE");

        //When
        Customer actual = customerRawMapper.mapRow(resultSet, 1);

        //Then
        Customer expected = new Customer(
                1, "Maria", "maria@gmail.com", 19, "FEMALE"
        );
        assertThat(actual).isEqualTo(expected);
    }
}
