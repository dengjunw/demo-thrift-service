package com.dengjunwu.server.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "thrift.server")
public class ThriftServerProperties {
    /**
     * port thrift used to bind.
     * find usable port if undefined.
     */

    private int port = 0;

    /**
     * thrift.threads.max
     */
    private int workThreads = 4 * Runtime.getRuntime().availableProcessors();

    /**
     * thrift.max-queued-requests
     */
    private int workerQueueCapacity = 1024;

    /**
     * thrift.io-threads.count
     */
    private int acceptorThreadCount = 2;

    /**
     * thrift.io-threads.count
     */
    private int ioThreads = 2 * Runtime.getRuntime().availableProcessors();

    /**
     * thrift.idle-connection-timeout
     */
    private String idelTimeout = "3s";
}
