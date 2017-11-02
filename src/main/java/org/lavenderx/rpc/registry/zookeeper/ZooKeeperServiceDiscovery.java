package org.lavenderx.rpc.registry.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.lavenderx.rpc.registry.ServiceDiscovery;

/**
 * 基于 ZooKeeper 的服务发现接口实现
 */
@Slf4j
public class ZooKeeperServiceDiscovery implements ServiceDiscovery {

    public ZooKeeperServiceDiscovery() {
    }

    @Override
    public String discover(String serviceName) {
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectionTimeoutMs(5000)
                .connectString("zkAddresses")
                .namespace(serviceName)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        client.start();

        try {
            return new String(client.getData().forPath(serviceName), "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}