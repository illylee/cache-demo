package com.example.cache.config;

import lombok.Getter;

@Getter
public enum CacheType {

    CUSTOMER("customerIdCache", 5 * 60, 10000),
    PRODUCT_DETAIL("productDetailCache", 5 * 60, 10000);

    CacheType(String name, int expiredAfterWrite, int maximumSize) {
        this.name = name;
        this.expiredAfterWrite = expiredAfterWrite;
        this.maximumSize = maximumSize;
    }

    private String name;
    private int expiredAfterWrite;
    private int maximumSize;

}