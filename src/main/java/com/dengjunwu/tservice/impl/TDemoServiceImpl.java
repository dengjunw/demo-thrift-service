package com.dengjunwu.tservice.impl;

import com.dengjunwu.exception.ThriftServiceException;
import com.dengjunwu.server.ThriftServiceService;
import com.dengjunwu.tservice.TDemoService;
import lombok.extern.slf4j.Slf4j;

@ThriftServiceService
@Slf4j
public class TDemoServiceImpl implements TDemoService {


    @Override
    public Integer count() throws ThriftServiceException {
        log.info("thrift method ....");
        return 5;
    }
}
