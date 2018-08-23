package com.dengjunwu.config;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Created by dengjunwu on 2018/4/1.
 */
@ApplicationPath("/")
public class ApiApplication extends ResourceConfig {
    public ApiApplication() {
        this.packages("com.dengjunwu");
        this.register(MultiPartFeature.class);
//        this.register(SimpleLoggerFilter.class);
//        this.register(SignFilter.class);
//        this.register(HeadValidateRequestFilter.class);
//        this.register(LoginValidateRequestFilter.class);
//        this.register(MobileValidateRequestFilter.class);
//        this.register(TraceIdMDCFilter.class);
//        this.register(CrossDomainFilter.class);

        this.setProperties(ImmutableMap.of("jersey.config.server.wadl.disableWadl",true));
    }
}
