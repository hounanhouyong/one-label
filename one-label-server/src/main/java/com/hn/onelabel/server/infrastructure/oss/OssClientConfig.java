package com.hn.onelabel.server.infrastructure.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
public class OssClientConfig {
    @Autowired
    private Environment env;
    @Autowired
    private OssProperties ossProperties;

    @Bean
    public OSS ossClient() {
        String endpoint = ossProperties.getInternalEndPoint();
        if ("loc".equals(env.getProperty("spring.profiles.active"))) {
            endpoint = ossProperties.getExternalEndPoint();
        }
        return new OSSClientBuilder().build(endpoint, ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
    }
}
