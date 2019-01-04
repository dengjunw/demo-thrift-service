package com.dengjunwu.server;

import com.alibaba.fastjson.JSONObject;
import com.facebook.nifty.core.RequestContext;
import com.facebook.swift.service.ThriftEventHandler;
import com.google.common.base.Joiner;
import io.airlift.units.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

@SwiftGlobalEventHandler
@Slf4j
public class LogEventHandler extends ThriftEventHandler {

    public Object getContext(String methodName, RequestContext requestContext) {
        if(requestContext.getContextData("traceId")!=null){
            MDC.put("traceId",requestContext.getContextData("traceId").toString());
        }else {
            MDC.put("traceId", TraceId.id());
        }
        return new LogEventHandler.PerCallMethodStats(requestContext);
    }

    public void preRead(Object context, String methodName) {
        ((LogEventHandler.PerCallMethodStats) context).preReadTime = System.currentTimeMillis();
    }

    public void postRead(Object context, String methodName, Object[] args) {
        LogEventHandler.PerCallMethodStats ctx = (LogEventHandler.PerCallMethodStats) context;
        ctx.methodName = methodName;
        ctx.argString = generateArgs(args);
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

    public void preWrite(Object context, String methodName, Object result) {
        long now = System.currentTimeMillis();
        LogEventHandler.PerCallMethodStats ctx = (LogEventHandler.PerCallMethodStats) context;
        ctx.preWriteTime = now;
        ctx.result = result;
    }

    public void preWriteException(Object context, String methodName, Throwable t) {
        this.preWrite(context, methodName, t);
        ((LogEventHandler.PerCallMethodStats) context).success = false;
    }

    public void postWrite(Object context, String methodName, Object result) {
    }

    public void postWriteException(Object context, String methodName, Throwable t) {
        this.postWrite(context, methodName, t);
    }

    public void done(Object context, String methodName) {
        LogEventHandler.PerCallMethodStats ctx = (LogEventHandler.PerCallMethodStats) context;
        Duration duration = Duration.nanosSince(ctx.startTime);
        log.info("duration {},{}", duration, ctx);
    }

    private class PerCallMethodStats {
        public final RequestContext requestContext;
        public boolean success = true;
        public long startTime = System.nanoTime();
        public long preReadTime;
        public long preWriteTime;
        public String methodName;
        public String argString;
        //        public Object[] args;
        public Object result;

        public PerCallMethodStats(RequestContext requestContext) {
            this.requestContext = requestContext;
        }

        @Override
        public String toString() {
            return "" + methodName + "(" + argString + ")" +
                    ", success=" + success +
                    ", result=\"" + resultString() + "\"" +
                    ", preReadTime=" + preReadTime +
                    ", preWriteTime=" + preWriteTime +
                    ", clientAddress=" + requestContext.getConnectionContext().getRemoteAddress().toString();
        }

        private String resultString() {
            if (result == null) {
                return "null";
            } else if (result instanceof Throwable) {
                return result.toString();
//                return ((Throwable) result).getMessage();
            } else {
                try {
                    return result.toString();
                } catch (NullPointerException e) {
                    return "null";
                }
            }
        }
    }
}
