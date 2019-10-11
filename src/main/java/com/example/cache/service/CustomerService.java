package com.example.cache.service;

import com.example.cache.domain.Customer;

public interface CustomerService {

    Customer selectCustomer(final Long customerId);

    void updateCustomer(Long customerId, String info);
}
