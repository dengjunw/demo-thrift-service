package com.dengjunwu.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SwiftGlobalEventHandler {
}
