package com.dengjunwu.exception;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.thrift.TApplicationException;

@ThriftStruct
@NoArgsConstructor
@Data
public class ThriftServiceException extends TApplicationException {

    @ThriftField(1)
    public String message;

    public ThriftServiceException(String message) {
        super(message);
        this.message = message;
    }

}