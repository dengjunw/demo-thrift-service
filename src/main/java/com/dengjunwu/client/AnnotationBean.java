package com.dengjunwu.client;

import com.dengjunwu.client.annotation.ThriftClient;
import com.dengjunwu.client.pool.TNiftyClientServicePool;
import com.dengjunwu.client.pool.ThriftClientKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


@Component
@Configuration
@Import({ThriftServiceConfiguration.class})
@Slf4j
public class AnnotationBean implements DisposableBean, BeanPostProcessor, ApplicationContextAware {

    private final ConcurrentMap<ThriftClientKey, ReferenceBean<?>> referenceConfigs = new ConcurrentHashMap<ThriftClientKey, ReferenceBean<?>>();

    TNiftyClientServicePool clientServicePool;

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.clientServicePool = applicationContext.getBean(TNiftyClientServicePool.class);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {
        Class clazz = bean.getClass();
        if(isProxyBean(bean)){
            clazz = AopUtils.getTargetClass(bean);
        }
        do {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    ThriftClient reference = field.getAnnotation(ThriftClient.class);
                    if (reference != null) {
                        Object value = refer(reference, field.getType());
                        if (value != null) {
                            field.set(bean, value);
                        }
                    }
                } catch (Exception e) {
                    throw new BeanInitializationException("Failed to init remote service reference at filed " + field.getName() + " in class " + bean.getClass().getName(), e);
                }
            }
            for (Method method : clazz.getDeclaredMethods()) {
                String name = method.getName();
                if (name.length() > 3 && name.startsWith("set")
                        && method.getParameterTypes().length == 1
                        && Modifier.isPublic(method.getModifiers())
                        && !Modifier.isStatic(method.getModifiers())) {
                    try {
                        ThriftClient reference = method.getAnnotation(ThriftClient.class);
                        if (reference != null) {
                            Object value = refer(reference, method.getParameterTypes()[0]);
                            if (value != null) {
                                method.invoke(bean, new Object[]{value});
                            }
                        }
                    } catch (Exception e) {
                        throw new BeanInitializationException("Failed to init remote service reference at method " + name + " in class " + bean.getClass().getName(), e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public void destroy() throws Exception {
        for (ReferenceBean<?> referenceConfig : referenceConfigs.values()) {
            try {
                referenceConfig.destroy();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private Object refer(ThriftClient reference, Class<?> referenceClass) { //method.getParameterTypes()[0]
        ThriftClientKey key = new ThriftClientKey(referenceClass, reference.application());
        ReferenceBean<?> referenceConfig = referenceConfigs.get(key);
        if (referenceConfig == null) {
            referenceConfig = new ReferenceBean<>();
            referenceConfig.setInterface(referenceClass);
            referenceConfig.setClientKey(key);
            referenceConfig.setClientServicePool(clientServicePool);
            referenceConfig.setThriftClient(reference);
            referenceConfig.setInit(reference.init());
            if (applicationContext != null) {
                try {
                    referenceConfig.afterPropertiesSet();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
            referenceConfigs.putIfAbsent(key, referenceConfig);
            referenceConfig = referenceConfigs.get(key);
        }
        return referenceConfig.get();
    }

    private boolean isProxyBean(Object bean) {
        return AopUtils.isAopProxy(bean);
    }
}
