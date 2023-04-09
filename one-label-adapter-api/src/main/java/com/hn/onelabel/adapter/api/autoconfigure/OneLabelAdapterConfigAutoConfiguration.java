package com.hn.onelabel.adapter.api.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableFeignClients(basePackages = {"com.hn.onelabel.adapter.api.feign"})
public class OneLabelAdapterConfigAutoConfiguration {
}
