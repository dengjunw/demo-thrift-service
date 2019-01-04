package com.dengjunwu.util;

import com.dengjunwu.server.TraceId;
import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;

@PreMatching
@Priority(Integer.MIN_VALUE)
public class TraceIdMDCFilter implements ContainerRequestFilter {
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		MDC.put("traceId", TraceId.id());
	}
}
