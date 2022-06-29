package com.sun.server;


import com.sun.common.util.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ImportResource;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;

@ImportResource(locations = {"classpath:./applicationContext.xml"})

// 禁用自动化配置数据源，避免加载不必要的自动化配置
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@EnableDiscoveryClient
public class ServiceApplication extends SpringBootServletInitializer {
    @PostConstruct
    void setTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(Constants.DEFAULT_TIME_ZONE)));
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ServiceApplication.class);
    }

    public static void main(String[] args )
    {
        // 设置关闭热部署
        //System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(ServiceApplication.class, args);
    }
}