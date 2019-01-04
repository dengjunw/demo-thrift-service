package com.dengjunwu.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.facebook.nifty.client.ClientRequestContext;
import com.facebook.swift.service.ThriftClientEventHandler;
import com.google.common.base.Joiner;
import io.airlift.units.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.System.nanoTime;

@Slf4j
public class ThriftClientLogHandler extends ThriftClientEventHandler {

    private static class LogContext
    {
        public boolean success = true;
        public long startTime = nanoTime();
        public String methodName;
        public String argString;
    }

    @Override
    public Object getContext(String methodName, ClientRequestContext requestContext) {
        return new ThriftClientLogHandler.LogContext();
    }

    @Override
    public void preWrite(Object context, String methodName, Object[] args) {
        long now = nanoTime();
        ThriftClientLogHandler.LogContext ctx = (ThriftClientLogHandler.LogContext)context;
        ctx.startTime = now;
        ctx.argString = generateArgs(args);
        ctx.methodName=methodName;
    }

    private String generateArgs(Object[] args) {
        Joiner joiner = Joiner.on(",");
        List<Object> list = new ArrayList<>();
        for (Object one : args) {
            if (one == null) {
                list.add("\"null\"");
            } else if (one instanceof String) {
                list.add("\"" + one + "\"");
            } else if (one.getClass().isPrimitive()) {
                list.add(one);
            } else if (isWrapClass(one.getClass())) {
                list.add(one);
            } else if (one instanceof List){
                list.add(JSONObject.toJSONString(one));
            }else {
                list.add(ToStringBuilder.reflectionToString(one, ToStringStyle.SHORT_PREFIX_STYLE));
            }
        }
        return joiner.join(list);
    }

    private boolean isWrapClass(Class clz) {
        try {
            return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
        } catch (Exception e) {
            return false;
        }
    }

    private static Duration nanosBetween(long start, long end)
    {
        return new Duration(end - start, TimeUnit.NANOSECONDS);
    }

    @Override
    public void done(Object context, String methodName) {
        ThriftClientLogHandler.LogContext ctx = (ThriftClientLogHandler.LogContext)context;
        log.info("duration {}ms,{}", Math.round(nanosBetween(ctx.startTime,nanoTime()).getValue()/1000000),
                JSON.toJSONString(context));
    }
}
