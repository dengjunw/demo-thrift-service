package com.dengjunwu.client.pool;

import com.dengjunwu.client.SwiftClientManager;
import com.dengjunwu.client.ThriftClientLogHandler;
import com.dengjunwu.client.config.ThriftServiceProperties;
import com.dengjunwu.client.exception.ThriftClientException;
import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.nifty.client.NiftyClientChannel;
import com.facebook.nifty.client.NiftyClientConnector;
import com.facebook.swift.service.ThriftClientManager;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.jboss.netty.channel.Channel;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TNiftyClientServicePool {

    private final GenericKeyedObjectPool<ThriftClientKey, NiftyClientChannel> pool;

    protected volatile SwiftClientManager swiftClientManager;

    public SwiftClientManager createClientManager() {
        return this.swiftClientManager != null ? this.swiftClientManager : this.pCreateManager();
    }

    private synchronized SwiftClientManager pCreateManager() {
        if (this.swiftClientManager != null) {
            return this.swiftClientManager;
        } else {
            this.swiftClientManager = new SwiftClientManager();
            return this.swiftClientManager;
        }
    }

    public <T> T getThriftClient(NiftyClientChannel clientChannel, Class<T> interfaceClass) throws Exception {
        ThriftClientManager clientManager = createClientManager().getThriftClientManager();
        T delegate = clientManager.createClient(clientChannel, interfaceClass, Lists.newArrayList(new ThriftClientLogHandler()));
        return delegate;
    }


    public TNiftyClientServicePool(ThriftServiceProperties config) {
        pool = new GenericKeyedObjectPool<>(new BaseKeyedPooledObjectFactory<ThriftClientKey, NiftyClientChannel>() {
            @Override
            public NiftyClientChannel create(ThriftClientKey key) throws Exception {
                if (log.isDebugEnabled()) {
                    log.debug("Connecting to {}", key);
                }
                String applicationName = key.getApplication();
                if (config.getApplications() == null || !config.getApplications().containsKey(applicationName)) {
                    log.error("no application named {} found from config.", applicationName);
                    throw new ThriftClientException("no application named '" + applicationName + "' found from config.");
                }
                ThriftClientManager clientManager = createClientManager().getThriftClientManager();
                HostAndPort hostAndPort = HostAndPort.fromString(config.getApplicationAddress(applicationName));
                NiftyClientConnector connector = new FramedClientConnector(hostAndPort);
                ListenableFuture<NiftyClientChannel> connectFuture = clientManager.createChannel(
                        connector,
                        config.getConnectTimeout(),
                        config.getReceiveTimeout(),
                        config.getReadTimeout(),
                        config.getWriteTimeout(),
                        config.getMaxFrameSize(),
                        config.getSocksProxy()
                );
                ListenableFuture<NiftyClientChannel> clientFuture = Futures.transform(connectFuture, new Function<NiftyClientChannel, NiftyClientChannel>() {
                    @Nullable
                    public NiftyClientChannel apply(@NotNull NiftyClientChannel channel) {
                        return channel;
                    }
                });
                return clientFuture.get(config.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS);
            }

            @Override
            public PooledObject<NiftyClientChannel> wrap(NiftyClientChannel value) {
                return new DefaultPooledObject<>(value);
            }

            @Override
            public boolean validateObject(ThriftClientKey key, PooledObject<NiftyClientChannel> p) {
                try {
                    Channel transport = p.getObject().getNettyChannel();
                    if (transport.isOpen()) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public void destroyObject(ThriftClientKey key, PooledObject<NiftyClientChannel> p) throws Exception {
                if (log.isDebugEnabled()) {
                    log.debug("Closing connection to " + key);
                }
                NiftyClientChannel channel = p.getObject();
                if (channel != null) {
                    channel.close();
                }
            }

        }, config.poolConfig());
    }


    public GenericKeyedObjectPool<ThriftClientKey, NiftyClientChannel> getPool() {
        return pool;
    }
}
