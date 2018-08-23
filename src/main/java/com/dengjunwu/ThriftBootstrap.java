package com.dengjunwu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by dengjunwu on 2018/8/23.
 */
@SpringBootApplication
//@ComponentScan(basePackages = {"com.dengjunwu", "com.cyril"},
//        excludeFilters={@ComponentScan.Filter(type = FilterType.REGEX,
//                pattern = {"com\\.dengjunwu\\.config\\.SpringConfig",
//                        "com\\.dengjunwu\\.config\\.JerseyConfig",
//                        "com\\.dengjunwu\\.ThriftBootstrap"})})
@ComponentScan(basePackages = {"com.dengjunwu", "com.cyril"})
@Slf4j
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class ThriftBootstrap {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ThriftBootstrap.class, args);
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        log.info("{} profile is active", activeProfiles);
    }
}
