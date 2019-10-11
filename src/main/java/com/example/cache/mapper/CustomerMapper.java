package com.example.cache.mapper;

import com.example.cache.domain.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CustomerMapper {

    Customer selectCustomer(Long customerId);
    void updateCustomer(@Param("customerId") Long customerId, @Param("info") String info);
}
