package com.dengjunwu.client;

import com.dengjunwu.client.config.ThriftServiceProperties;
import com.dengjunwu.client.pool.TNiftyClientServicePool;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.inject.Singleton;


@Configuration
@EnableConfigurationProperties(ThriftServiceProperties.class)
@AutoConfigureOrder
public class ThriftServiceConfiguration {

    @Bean
    @Singleton
    @Scope(value = "singleton")
    public TNiftyClientServicePool niftyClientServicePool(ThriftServiceProperties config) {
        return new TNiftyClientServicePool(config);
    }
}
