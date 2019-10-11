package com.example.cache.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Customer {

    private Long customerId;
    private String email;
    private String info;

    public Customer(Long customerId, String email, String info) {
        this.customerId = customerId;
        this.email = email;
        this.info = info;
    }

}
