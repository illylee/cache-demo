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
//@CacheConfig(cacheNames = {"customerIdCache"})
//@CacheConfig: 클래스 단위로 캐시설정을 동일하게 하는데 사용
//이 설정은 CacheManager가 여러개인 경우에만 사용
//Member조회 클래스에서는 Redis기반 캐시를 사용하고 Product 조회 클래스에서는 EHCache 기반 캐시를 사용할 때 각 클래스 별로 CacheManager를 지정 가능
//cacheNames: 캐시 명
//cacheManager: 사용할 CacheManager를 지정 (EHCacheManager, RedisCacheManager등)
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    CustomerMapper customerMapper;

    //@CachePut: 메서드 실행에 영향을 주지 않고 캐시를 갱신해야하는 경우.
    //보통은 @Cacheable과 @CachePut Annotation을 같이 사용하지 않는다. (둘은 다른 동작을 하기 때문에, 실행순서에 따라 다른 결과가 나올 수 있다.)
    //@CachePut Annotation은 캐시 생성용으로만 사용한다.


    //@Cacheable: 캐싱할 수 있는 메서드를 지정하기 위해 사용
    //value: 캐시명
    //key: 같은 캐시명을 사용할 때, 구분되는 구분 값. KeyGenerator와 같이 쓸 수 없음.
    //keyGenerator: 특정 로직에 의해 cache key를 만든고자 하는 경우. 4.0 이후 버전부터는 SimpleKeyGenerator 사용.
    //cacheManaager: 사용할 cacheManager 지정.
    //cacheResolver: cache key에 대한 결과값을 돌려주는 Resolver (interceptor역할)
    //condition: SpEL 표현식을 통해 특정 조건에 부합하는 경우에만 캐시 사용. 연산조건이 true인 경우에만 캐싱
    //unless: 캐싱이 이루어지지않는 조건을 설정. 연산조건이 true이면 캐싱되지 않는다. ex) id가 널이 아닌경우에만 캐싱. (unless="#id == null")
    //sync: 캐시 구현체가 Thread safe 하지 않는 경우, 자체적으로 캐시에 동기화를 거는 속성.
    @Cacheable(key = "'customer_id_' + #customerId", value = "customerIdCache")
    public Customer selectCustomer(Long customerId) {

        return customerMapper.selectCustomer(customerId);


    }


    /**
     * 변경이 일어나면 cache evict
     * @param customerId
     * @param info
     */
    //@CacheEvict: 메서드 실행 시, 해당 캐시를 삭제
    //beforeInvocation: true면 메서드 실행 이전에 캐시 삭제, false면 메서드 실행 이후 삭제. default: false
    @CacheEvict(key="'customer_id_' + #customerId", value = "customerIdCache")
    public void updateCustomer(Long customerId, String info) {
         customerMapper.updateCustomer(customerId, info);
    }

}
