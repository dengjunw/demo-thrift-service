package com.dengjunwu.server;

import com.dengjunwu.server.autoconfigure.ThriftServerProperties;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.service.*;
import io.airlift.units.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SwiftServerRunner implements CommandLineRunner, DisposableBean {
    private final List<Object> serviceList = new ArrayList<>();

    @Autowired
    AbstractApplicationContext applicationContext;

    @Autowired
    ThriftServerProperties thriftServerProperties;

    private ThriftServer server;

    @Override
    public void destroy() throws Exception {
        log.info("Shutting down thrift server ...");
        Optional.ofNullable(server).ifPresent(ThriftServer::close);
        log.info("thrift server stopped.");
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("Starting thrift Server ...");

        List<ThriftEventHandler> globalInterceptors = getBeanNamesByTypeWithAnnotation(SwiftGlobalEventHandler.class, ThriftEventHandler.class)
                .map(name -> applicationContext.getBeanFactory().getBean(name, ThriftEventHandler.class))
                .collect(Collectors.toList());

        getBeanNamesByAnnotation(ThriftServiceService.class, ThriftService.class)
                .forEach(
                        name -> {
                            Object srv = applicationContext.getBeanFactory().getBean(name);
                            serviceList.add(srv);
                            log.info("'{}' service has been registered.", srv.getClass().getName());
                        }
                );
        if (serviceList.size() == 0) {
            log.error("no thrift server service found.");
        } else {
            ThriftServiceProcessor processor = new ThriftServiceProcessor(new ThriftCodecManager(), globalInterceptors, serviceList);
            server = new ThriftServer(processor, constractConfig());
            startDaemonAwaitThread();
        }
    }

    private void startDaemonAwaitThread() {
        Thread awaitThread = new Thread(() -> SwiftServerRunner.this.server.start());
        awaitThread.setDaemon(false);
        awaitThread.start();
    }

    private ThriftServerConfig constractConfig() {
        ThriftServerConfig config = new ThriftServerConfig();
        config.setPort(thriftServerProperties.getPort());
        config.setAcceptorThreadCount(thriftServerProperties.getAcceptorThreadCount());
        config.setIoThreadCount(thriftServerProperties.getIoThreads());
        config.setWorkerThreads(thriftServerProperties.getWorkThreads());
        config.setMaxQueuedRequests(thriftServerProperties.getWorkerQueueCapacity());
        config.setIdleConnectionTimeout(Duration.valueOf(thriftServerProperties.getIdelTimeout()));
        return config;
    }

    private Stream<String> getBeanNamesByAnnotation(Class<? extends Annotation> annotationType, Class<? extends Annotation> beanType) throws Exception {

        return Stream.of(applicationContext.getBeanNamesForAnnotation(beanType))
                .filter(name -> {
                    final BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
                    final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotationType);

                    if (!beansWithAnnotation.isEmpty()) {
                        return beansWithAnnotation.containsKey(name);
                    } else if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                        StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                        return metadata.isAnnotated(annotationType.getName());
                    }

                    return false;
                });
    }

    private <T> Stream<String> getBeanNamesByTypeWithAnnotation(Class<? extends Annotation> annotationType, Class<T> beanType) throws Exception {

        return Stream.of(applicationContext.getBeanNamesForType(beanType))
                .filter(name -> {
                    final BeanDefinition beanDefinition = applicationContext.getBeanFactory().getBeanDefinition(name);
                    final Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(annotationType);

                    if (!beansWithAnnotation.isEmpty()) {
                        return beansWithAnnotation.containsKey(name);
                    } else if (beanDefinition.getSource() instanceof StandardMethodMetadata) {
                        StandardMethodMetadata metadata = (StandardMethodMetadata) beanDefinition.getSource();
                        return metadata.isAnnotated(annotationType.getName());
                    }

                    return false;
                });
    }
}
