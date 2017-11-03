package org.lavenderx.rpc.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan({"org.lavenderx.rpc.server", "org.lavenderx.rpc.registry"})
@PropertySource({"classpath:rpc.properties"})
public class RpcConfig {

    @Value("${rpc.registry_addresses}")
    private String zkAddresses;

    @Bean(destroyMethod = "close")
    public CuratorFramework zkClient() {
        return CuratorFrameworkFactory.newClient(zkAddresses, new ExponentialBackoffRetry(1000, 3));
    }
}
