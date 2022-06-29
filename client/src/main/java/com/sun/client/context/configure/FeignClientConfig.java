package com.sun.client.context.configure;

import feign.Contract;
import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: Feign Client Beans
 * @author: Sun Xiaodong
 */
@Configuration
public class FeignClientConfig {
    @Bean
    public Contract feignContract() {
        return new Contract.Default();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        // NONE：默认级别，不显示任何日志
        // BASIC：仅记录请求方法、URL、相应状态码和执行时间
        // HEADERS：除了BASIC中定义的信息之外，还有请求和响应的头信息
        // FULL：除了HEADERS中定义的信息之外，还有请求和响应的征文及元数据
        return Logger.Level.FULL;
    }
}
