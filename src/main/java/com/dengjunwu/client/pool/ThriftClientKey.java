package com.dengjunwu.client.pool;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode
@Data
@AllArgsConstructor
public class ThriftClientKey {

    private Class clazz;
    private String application;
}
