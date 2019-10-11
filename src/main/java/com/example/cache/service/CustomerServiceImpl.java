package com.example.cache.service;

import com.example.cache.domain.Customer;
import com.example.cache.mapper.CustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@CacheConfig(cacheNames = "customer")
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerMapper customerMapper;

    @Cacheable(key="#customerId")
    public Customer selectCustomer(Long customerId) {
        return customerMapper.selectCustomer(customerId);
    }


    /**
     * 변경이 일어나면 cache evict
     * @param customerId
     * @param info
     */
    @CacheEvict(value = "customer", key="#customerId" )
    public void updateCustomer(Long customerId, String info) {
         customerMapper.updateCustomer(customerId, info);
    }


}
