package com.dengjunwu.config;

import com.dengjunwu.util.SimpleLoggerFilter;
import com.dengjunwu.util.TraceIdMDCFilter;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        this.packages("com.dengjunwu");
        this.register(SimpleLoggerFilter.class);
        this.register(TraceIdMDCFilter.class);
    }
}