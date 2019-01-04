package com.dengjunwu;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;

import java.security.Security;

/**
 * Created by dengjunwu on 2018/8/23.
 */
@SpringBootApplication
//@ComponentScan(basePackages = {"com.dengjunwu", "com.cyril"},
//        excludeFilters={@ComponentScan.Filter(type = FilterType.REGEX,
//                pattern = {"com\\.dengjunwu\\.config\\.SpringConfig",
//                        "com\\.dengjunwu\\.config\\.JerseyConfig",
//                        "com\\.dengjunwu\\.ThriftBootstrap"})})
@Slf4j
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@ActiveProfiles(profiles = {"test"})
public class ThriftBootstrap {
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        ApplicationContext context = SpringApplication.run(ThriftBootstrap.class, args);
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();

        log.info("{} profile is active", activeProfiles);
    }
}
