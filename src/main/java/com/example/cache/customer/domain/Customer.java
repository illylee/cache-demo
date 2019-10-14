package com.example.cache.customer.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerId;
    private String email;
    private String info;

    public Customer(Long customerId, String email, String info) {
        this.customerId = customerId;
        this.email = email;
        this.info = info;
    }

}
