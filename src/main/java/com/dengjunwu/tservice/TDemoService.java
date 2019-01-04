package com.dengjunwu.tservice;

import com.dengjunwu.exception.ThriftServiceException;
import com.facebook.swift.service.ThriftException;
import com.facebook.swift.service.ThriftMethod;
import com.facebook.swift.service.ThriftService;

@ThriftService
public interface TDemoService {

    @ThriftMethod(exception = {@ThriftException(type = ThriftServiceException.class, id = 1)})
    Integer count() throws ThriftServiceException;
}
