package com.example.cache.customer.controller;

import com.example.cache.customer.domain.Customer;
import com.example.cache.customer.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/v1/customer")
@Slf4j
public class CustomerController {
    @Autowired
    CustomerService customerService;


    @RequestMapping(value = "", method = RequestMethod.GET)
    public Customer getCustomer(@RequestParam Long customerId) {
        long start = System.currentTimeMillis();
        Customer customer = customerService.selectCustomer(customerId);
        log.info("customer: {}", customer.toString());
        long end = System.currentTimeMillis();
        log.info("수행시간: {}", end - start);
        return customer;
    }

    @RequestMapping(value = "/edit", method = RequestMethod.GET)
    public void editCustomer(@RequestParam Long customerId, @RequestParam String info) {
        customerService.updateCustomer(customerId, info);
    }

    @RequestMapping(value = "/evict", method = RequestMethod.GET)
    public void evictTest(@RequestParam String key) {
        customerService.evict(key);
    }



}
