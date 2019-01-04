package com.dengjunwu.client.config;

import com.google.common.net.HostAndPort;
import io.airlift.units.Duration;
import lombok.Data;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties(prefix = "thrift.client")
public class ThriftServiceProperties {

    HashMap<String, String> applications;
    int poolMaxTotal = 512;
    int poolMaxTotalPerKey = 8;
    int poolMaxIdlePerKey = 6;
    int poolMinIdlePerKey = 2;
    long poolMaxWait = 1000;
    long timeBetweenEvictionRunsMillis = 10;
    boolean blockWhenExhausted = true;
    boolean lifo = false;

    Duration connectTimeout = new Duration(1, TimeUnit.SECONDS);
    Duration receiveTimeout = new Duration(1, TimeUnit.SECONDS);
    Duration readTimeout = new Duration(1, TimeUnit.SECONDS);
    Duration writeTimeout = new Duration(1, TimeUnit.SECONDS);
    int maxFrameSize = 16777216;
    HostAndPort socksProxy;

    public String getApplicationAddress(String key) {
        return applications.get(key);
    }

    public GenericKeyedObjectPoolConfig poolConfig() {
        GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
        poolConfig.setMaxTotal(poolMaxTotal);
        poolConfig.setMaxTotalPerKey(poolMaxTotalPerKey);
        poolConfig.setMaxIdlePerKey(poolMaxIdlePerKey);
        poolConfig.setMinIdlePerKey(poolMinIdlePerKey);
        poolConfig.setMaxWaitMillis(poolMaxWait);
        poolConfig.setBlockWhenExhausted(blockWhenExhausted);
        poolConfig.setLifo(lifo);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis * 1000);
        return poolConfig;
    }
}
