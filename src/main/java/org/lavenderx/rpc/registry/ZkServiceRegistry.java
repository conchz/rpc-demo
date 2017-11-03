package org.lavenderx.rpc.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.lavenderx.rpc.domain.ServiceDetail;
import org.lavenderx.rpc.registry.ServiceRegistry;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * 基于 ZooKeeper 的服务注册接口实现: http://ifeve.com/zookeeper-curato-framework/
 * Zookeeper客户端Curator使用详解: http://www.jianshu.com/p/70151fc0ef5d
 * http://blog.csdn.net/sqh201030412/article/details/51438508
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {

    @Override
    public void register(String serviceName, String zkAddresses) {
        CuratorFramework client = CuratorFrameworkFactory.newClient(zkAddresses, new ExponentialBackoffRetry(1000, 3));
        client.start();

        try {
            client.create().forPath(serviceName, "Create init".getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ServiceDiscovery<ServiceDetail> serviceDiscovery;

    private final CuratorFramework client;

    public ZkServiceRegistry(CuratorFramework client, String basePath) {
        this.client = client;
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDetail.class)
                .client(client)
                .serializer(new JsonInstanceSerializer<>(ServiceDetail.class))
                .basePath(basePath)
                .build();
    }

    public void updateService(ServiceInstance<ServiceDetail> instance) throws Exception {
        serviceDiscovery.updateService(instance);
    }

    public void registerService(ServiceInstance<ServiceDetail> instance) throws Exception {
        serviceDiscovery.registerService(instance);
    }

    public void unregisterService(ServiceInstance<ServiceDetail> instance) throws Exception {
        serviceDiscovery.unregisterService(instance);
    }

    public Collection<ServiceInstance<ServiceDetail>> queryForInstances(String name) throws Exception {
        return serviceDiscovery.queryForInstances(name);
    }

    public ServiceInstance<ServiceDetail> queryForInstance(String name, String id) throws Exception {
        return serviceDiscovery.queryForInstance(name, id);
    }

    public void start() throws Exception {
        serviceDiscovery.start();
    }

    public void close() throws Exception {
        serviceDiscovery.close();
    }
}