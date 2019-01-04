package com.dengjunwu.server.autoconfigure;

import com.dengjunwu.server.SwiftServerRunner;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableConfigurationProperties(ThriftServerProperties.class)
@AutoConfigureOrder
public class ThriftAutoConfiguration {

    @Bean
    @ConditionalOnBean(annotation = {com.dengjunwu.server.ThriftServiceService.class})
    public SwiftServerRunner thriftServerRunner() {
        return new SwiftServerRunner();
    }
}
