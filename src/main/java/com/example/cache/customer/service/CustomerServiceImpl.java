package com.example.cache.customer.service;

import com.example.cache.customer.domain.Customer;
import com.example.cache.customer.mapper.CustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerMapper customerMapper;

    @Cacheable(key = "'customer_id_' + #customerId", value = "customerIdCache")
    public Customer selectCustomer(Long customerId) {
        return customerMapper.selectCustomer(customerId);
    }


    /**
     * 변경이 일어나면 cache evict
     * @param customerId
     * @param info
     */
    @CacheEvict(key="'customer_id_' + #customerId", value = "customerIdCache")
    public void updateCustomer(Long customerId, String info) {
         customerMapper.updateCustomer(customerId, info);
    }

}
