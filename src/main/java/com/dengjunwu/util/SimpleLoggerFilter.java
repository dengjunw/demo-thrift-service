package com.dengjunwu.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Priority;
import javax.ws.rs.container.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@PreMatching
@Priority(Byte.MIN_VALUE)
@Slf4j
@Component
public class SimpleLoggerFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Value("${environment:online}")
    String environment;

    private static final String REQUEST_TIME = "request-time";
    private static final String OPERATION_COST_TIME = "cost-time";
    private static final String URL = "url";
    private static final String METHOD = "method";
    private static final String HEAD = "head";
    private static final String ENTITY = "entity";
    private static final String STATUS = "status";

    SerializerFeature[] features = new SerializerFeature[]{SerializerFeature.WriteNullStringAsEmpty,
            SerializerFeature.DisableCircularReferenceDetect};

    private static final List<String> HEAD_NOT_INCLUDE = Arrays.asList("Accept", "Accept-Encoding", "Accept-Charset",
            "Accept-Language", "Connection", "Content-Encoding", "Content-Type", "Vary", "Cache-Control",
            "Host", "accept", "accept-encoding", "accept-charset", "accept-language", "connection", "content-encoding",
            "content-type", "vary", "cache-control", "host");

    private synchronized void setTime(ContainerRequestContext context) {
        if (context.getProperty(REQUEST_TIME) == null) {
            context.setProperty(REQUEST_TIME, System.currentTimeMillis());
        }
    }

    private void setRequestUrl(Map<String, Object> b, ContainerRequestContext requestContext) {
        b.put(METHOD, requestContext.getMethod());
        b.put(URL, requestContext.getUriInfo().getRequestUri().toASCIIString());
    }

    private void setRequestHeaders(Map<String, Object> b, MultivaluedMap<String, String> multivaluedMap) {
        Map<String, List<String>> newMap = new HashMap<String, List<String>>();
        for (Entry<String, List<String>> e : multivaluedMap.entrySet()) {
            String header = e.getKey();
            if (!HEAD_NOT_INCLUDE.contains(header)) {
                newMap.put(header, e.getValue());
            }
        }
        b.put(HEAD, newMap);
    }

    private void setEntity(Map<String, Object> b, byte[] entity) {
        if (entity.length == 0)
            return;
        b.put(ENTITY, new String(entity));
    }

    public void filter(ContainerRequestContext context) throws IOException {
        final Map<String, Object> b = new HashMap<String, Object>();
        try {
            setTime(context);
            setRequestHeaders(b, context.getHeaders());
            setRequestUrl(b, context);
            if (context.hasEntity()) {
                if (context != null && context.getMediaType() != null && context.getMediaType().getType() != null
                        && context.getMediaType().getType().equals("multipart")) {
                    b.put(ENTITY, new String("may be a picture"));
                } else {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    InputStream in = context.getEntityStream();
                    if (in.available() > 0) {
                        ReaderWriter.writeTo(in, out);
                        byte[] requestEntity = out.toByteArray();
                        setEntity(b, requestEntity);
                        context.setEntityStream(new ByteArrayInputStream(requestEntity));
                    }
                }
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            log.info(JSON.toJSONString(b, features));
        }
    }

    public void filter(ContainerRequestContext context, ContainerResponseContext responseContext) throws IOException {

        boolean showEntity = false;
        if (responseContext.getMediaType() != null) {
            responseContext.getMediaType().getType();
            if (responseContext.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE)
                    || responseContext.getMediaType().isCompatible(MediaType.APPLICATION_XML_TYPE)
                    || responseContext.getMediaType().isCompatible(MediaType.TEXT_HTML_TYPE)
                    || responseContext.getMediaType().isCompatible(MediaType.TEXT_XML_TYPE)
                    || responseContext.getMediaType().isCompatible(MediaType.TEXT_PLAIN_TYPE)) {
                showEntity = true;
            }
        }

        final Map<String, Object> b = new HashMap<String, Object>();
        b.put(STATUS, responseContext.getStatus());
        long startTime = 0;
        if (context.getProperty(REQUEST_TIME) != null) {
            startTime = (long) context.getProperty(REQUEST_TIME);
        }
        long time = System.currentTimeMillis() - startTime;
        b.put(OPERATION_COST_TIME, time);

        if (responseContext.hasEntity()) {
            if (showEntity) {
                b.put(ENTITY, responseContext.getEntity());
            } else {
                b.put(ENTITY, "not supported type");
            }
        }

        String logDeatil = JSON.toJSONString(b, features);
        if (logDeatil.length() >= 200 && StringUtils.equalsIgnoreCase(environment,"online")) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(StringUtils.substring(logDeatil, 0, 100));
            stringBuffer.append("......");
            stringBuffer.append(StringUtils.substring(logDeatil, logDeatil.length() - 30, logDeatil.length()));
            log.info(stringBuffer.toString());
        }else{
            log.info(logDeatil);
        }
    }
}