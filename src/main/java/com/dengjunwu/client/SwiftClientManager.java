package com.dengjunwu.client;

import com.facebook.swift.service.ThriftClientManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.PreDestroy;

@Slf4j
public class SwiftClientManager implements DisposableBean {

    private volatile ThriftClientManager thriftClientManager;

    public SwiftClientManager() {
        this.thriftClientManager = new ThriftClientManager();
    }

    public ThriftClientManager getThriftClientManager() {
        return thriftClientManager;
    }

    @Override
    @PreDestroy
    public void destroy() throws Exception {
        if (this.thriftClientManager != null) {
            this.thriftClientManager.close();
        }
    }
}
