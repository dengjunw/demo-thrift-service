package com.dengjunwu.module.Impl;

import com.cyril.springboot.thrift.server.ThriftServerService;
import com.dengjunwu.module.TDemoTest;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by dengjunwu on 2018/8/23.
 */
@ThriftServerService
@Slf4j
public class TDemoTestImpl implements TDemoTest {

    @Override
    public boolean isValidate() {
        return false;
    }
}
