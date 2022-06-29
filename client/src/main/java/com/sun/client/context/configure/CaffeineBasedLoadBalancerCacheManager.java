package com.sun.client.context.configure;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.sun.common.util.StringUtil;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheProperties;
import org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier;

/**
 * @description:
 * @author: Sun Xiaodong
 */
public class CaffeineBasedLoadBalancerCacheManager extends CaffeineCacheManager {

    public CaffeineBasedLoadBalancerCacheManager(LoadBalancerCacheProperties properties) {
        this(CachingServiceInstanceListSupplier.SERVICE_INSTANCE_CACHE_NAME, properties);
    }

    public CaffeineBasedLoadBalancerCacheManager(String cacheName, LoadBalancerCacheProperties properties) {
        super(new String[] {cacheName});
        if (!StringUtil.isBlank(properties.getCaffeine().getSpec())) {
            this.setCacheSpecification(properties.getCaffeine().getSpec());
        } else {
            // 没有自定义的缓存配置，则使用默认的缓存配置
            this.setCaffeine(Caffeine.newBuilder()
                                     .initialCapacity(properties.getCapacity())
                                     .expireAfterWrite(properties.getTtl())
                                     .softValues());
        }
    }
}
