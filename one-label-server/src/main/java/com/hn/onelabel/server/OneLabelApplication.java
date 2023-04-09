package com.hn.onelabel.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.web.client.RestTemplate;

@EnableRetry
@EnableAsync
@SpringBootApplication(scanBasePackages = {"com.hn.onelabel"},exclude = {SecurityAutoConfiguration.class, RestClientAutoConfiguration.class})
@MapperScan({"com.hn.onelabel.server.infrastructure.db.mapper"})
public class OneLabelApplication {

    public static void main(String[] args) {
        //解决es的netty冲突问题
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(OneLabelApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
