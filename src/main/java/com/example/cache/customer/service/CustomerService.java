package com.example.cache.customer.service;

import com.example.cache.customer.domain.Customer;

public interface CustomerService {

    Customer selectCustomer(final Long customerId);

    void updateCustomer(Long customerId, String info);
}
