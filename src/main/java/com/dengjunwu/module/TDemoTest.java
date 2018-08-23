package com.dengjunwu.module;


import com.cyril.springboot.thrift.server.ThriftServerService;
import com.facebook.swift.service.ThriftMethod;

/**
 * Created by dengjunwu on 2018/8/23.
 */
@ThriftServerService
public interface TDemoTest {
    @ThriftMethod
    boolean isValidate();
}
