package com.dengjunwu.module;


import com.cyril.springboot.thrift.server.ThriftServerService;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;
import org.apache.thrift.TApplicationException;

/**
 * Created by dengjunwu on 2018/8/23.
 */
@ThriftService
public interface TDemoTest {
    @ThriftMethod
    boolean isValidate() throws TApplicationException;
}
