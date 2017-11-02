package org.lavenderx.rpc.registry.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.lavenderx.rpc.registry.ServiceRegistry;

import java.nio.charset.StandardCharsets;

/**
 * 基于 ZooKeeper 的服务注册接口实现: http://ifeve.com/zookeeper-curato-framework/
 * Zookeeper客户端Curator使用详解: http://www.jianshu.com/p/70151fc0ef5d
 * http://blog.csdn.net/sqh201030412/article/details/51438508
 */
@Slf4j
public class ZooKeeperServiceRegistry implements ServiceRegistry {

    @Override
    public void register(String serviceName, String zkAddresses) {
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectionTimeoutMs(5000)
                .connectString(zkAddresses)
                .namespace(serviceName)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        client.start();

        try {
            client.create().forPath(serviceName, "Create init".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}