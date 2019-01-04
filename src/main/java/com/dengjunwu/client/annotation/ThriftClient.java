package com.dengjunwu.client.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ThriftClient {

    String application() default "default";
    int retries() default 3;
    boolean init() default  false;

}
