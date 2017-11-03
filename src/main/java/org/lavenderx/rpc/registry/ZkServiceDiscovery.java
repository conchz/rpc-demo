package org.lavenderx.rpc.registry;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;
import org.lavenderx.rpc.domain.ServiceDetail;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 ZooKeeper 的服务发现接口实现
 */
@Slf4j
public class ZkServiceDiscovery implements org.lavenderx.rpc.registry.ServiceDiscovery {

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

    private final ServiceDiscovery<ServiceDetail> serviceDiscovery;
    private final ConcurrentHashMap<String, ServiceProvider<ServiceDetail>> serviceProviderMap = new ConcurrentHashMap<>();

    public ZkServiceDiscovery(CuratorFramework client, String basePath) {
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceDetail.class)
                .client(client)
                .basePath(basePath)
                .serializer(new JsonInstanceSerializer<>(ServiceDetail.class))
                .build();
    }

    /**
     * Note: When using Curator 2.x (Zookeeper 3.4.x) it's essential that service provider objects are cached by your application and reused.
     * Since the internal NamespaceWatcher objects added by the service provider cannot be removed in Zookeeper 3.4.x,
     * creating a fresh service provider for each call to the same service will eventually exhaust the memory of the JVM.
     */
    public ServiceInstance<ServiceDetail> getServiceProvider(String serviceName) throws Exception {
        ServiceProvider<ServiceDetail> provider = serviceProviderMap.get(serviceName);
        if (provider == null) {
            provider = serviceDiscovery.serviceProviderBuilder().
                    serviceName(serviceName).
                    providerStrategy(new RandomStrategy<>())
                    .build();

            ServiceProvider<ServiceDetail> oldProvider = serviceProviderMap.putIfAbsent(serviceName, provider);
            if (oldProvider != null) {
                provider = oldProvider;
            } else {
                provider.start();
            }
        }

        return provider.getInstance();
    }

    public void start() throws Exception {
        serviceDiscovery.start();
    }

    public void close() throws IOException {
        for (Map.Entry<String, ServiceProvider<ServiceDetail>> entry : serviceProviderMap.entrySet()) {
            try {
                entry.getValue().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        serviceDiscovery.close();
    }
}