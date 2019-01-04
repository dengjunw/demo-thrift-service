package com.dengjunwu.client;

import com.dengjunwu.client.annotation.ThriftClient;
import com.dengjunwu.client.exception.ThriftClientException;
import com.dengjunwu.client.pool.TNiftyClientServicePool;
import com.dengjunwu.client.pool.ThriftClientKey;
import com.facebook.nifty.client.NiftyClientChannel;
import com.facebook.swift.service.RuntimeTTransportException;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.transport.TTransportException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

@Slf4j
public class ReferenceBean<T> implements FactoryBean<T>, InitializingBean, DisposableBean {

    private ThriftClientKey thriftClientKey;
    private ThriftClient thriftClient;
    private volatile TNiftyClientServicePool clientServicePool;
    private Class<T> interfaceClass;
    private Boolean init = false;
    private transient volatile T ref;
    private transient volatile boolean initialized;
    private transient volatile boolean destroyed;


    public synchronized void destroy() {
        if (ref == null) {
            return;
        }
        if (destroyed) {
            return;
        }
        destroyed = true;
        ref = null;
    }

    @Override
    public T getObject() throws Exception {
        return get();
    }

    @Override
    public Class<?> getObjectType() {
        return getInterfaceClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Boolean b = isInit();
        if (b != null && b.booleanValue()) {
            getObject();
        }
    }

    public void setClientKey(ThriftClientKey clientKey) {
        this.thriftClientKey = clientKey;
    }

    public synchronized T get() {

        if (destroyed) {
            throw new IllegalStateException("Already destroyed!");
        }
        if (ref == null) {
            init();
        }
        return ref;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterface(Class interfaceClass) {
        this.interfaceClass = interfaceClass;
    }


    public void setClientServicePool(TNiftyClientServicePool clientServicePool) {
        this.clientServicePool = clientServicePool;
    }

    public void setThriftClient(ThriftClient thriftClient) {
        this.thriftClient = thriftClient;
    }

    public Boolean isInit() {
        return init;
    }

    public void setInit(Boolean init) {
        this.init = init;
    }

    private void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            ref = createJavassistDynamicProxy();
        } catch (IllegalAccessException | InstantiationException e) {
            log.error("fail to create proxy", e);
        }
    }

    private T createJavassistDynamicProxy() throws IllegalAccessException, InstantiationException {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(new Class[]{interfaceClass});
        Class proxyClass = proxyFactory.createClass();
        T javassistProxy = (T) proxyClass.newInstance();
        ((ProxyObject) javassistProxy).setHandler(new ReferenceBean.JavaAssitInterceptor());
        return javassistProxy;
    }

    private class JavaAssitInterceptor implements MethodHandler {
        public Object invoke(Object self, Method m, Method proceed,
                             Object[] args) throws Throwable {
            try {
                return invokeRemoteMethod(m, args);
            } catch (ThriftClientException e) {
                throw e.getCause();
            }
        }
    }

    private Object invokeRemoteMethod(Method m,
                                      Object[] args) {
        GenericKeyedObjectPool<ThriftClientKey, NiftyClientChannel> pool = clientServicePool.getPool();
        int index = 0;
        String beanName = interfaceClass.getName();
        while (index++ < thriftClient.retries()) {
            NiftyClientChannel clientChannel = null;
            try {
                clientChannel = pool.borrowObject(thriftClientKey);
                T delegate = clientServicePool.getThriftClient(clientChannel, interfaceClass);
                return m.invoke(delegate, args);
            } catch (UndeclaredThrowableException | InvocationTargetException e) {
                Throwable targetException;
                if (e instanceof UndeclaredThrowableException) {
                    targetException = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
                } else {
                    Throwable innerException = ((InvocationTargetException) e).getTargetException();
                    targetException = innerException.getCause() == null ? innerException : innerException.getCause();
                }
                if (targetException instanceof RuntimeTTransportException) {
                    targetException = targetException.getCause();
                }

                if (targetException instanceof TTransportException) {
                    Throwable realException = targetException.getCause();
                    if (realException instanceof SocketTimeoutException) { // 超时,直接抛出异常,不进行重试
                        throw new ThriftClientException(
                                ExceptionUtils.getMessage(e) + ", bean name is " + beanName, e);
                    } else if (realException == null) {
                        pool.clear(thriftClientKey);// 把以前的对象池进行销毁
                        handlerException(index, thriftClient.retries(), beanName, targetException);
                        continue;
                    } else if (realException instanceof SocketException) {
                        pool.clear(thriftClientKey);// 把以前的对象池进行销毁
                        handlerException(index, thriftClient.retries(), beanName, realException);
                        continue;
                    } else {
                        handlerException(index, thriftClient.retries(), beanName, targetException);
                        continue;
                    }
                } else if (targetException instanceof TApplicationException) {
                    throw new ThriftClientException(
                            ExceptionUtils.getMessage(e) + ", bean name is " + beanName, targetException);
                } else {
                    log.error("un know exception,do not retry.", targetException);
                    throw new ThriftClientException(
                            ExceptionUtils.getMessage(e) + ", bean name is " + beanName, targetException);
                }
            } catch (Exception e) {
                pool.clear(thriftClientKey);
                handlerException(index, thriftClient.retries(), beanName, e);
                continue;
            } finally {
                if (pool != null && clientChannel != null) {
                    pool.returnObject(thriftClientKey, clientChannel);
                }
            }
        }
        throw new ThriftClientException("");
    }

    private void handlerException(int index, int retryTimes, String beanName, Throwable t) {
        log.error("handlerException for the " + index + " times . total retryTimes is " + retryTimes + ". ", t);
        if (index == retryTimes) {
            log.error("fail to get " + beanName + " after " + index + " reties.stop retry.", t);
            throw new ThriftClientException(ExceptionUtils.getMessage(t) + ", bean name is " + beanName,
                    t);
        }
    }
}
