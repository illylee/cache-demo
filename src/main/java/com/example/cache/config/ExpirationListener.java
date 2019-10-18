package com.example.cache.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExpirationListener implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String key = new String(message.getBody());
        log.info("expired key: {}", key);
    }
}