package com.hn.onelabel.server.infrastructure.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("oss.client.config")
public class OssProperties {
    private String externalEndPoint;
    private String internalEndPoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String imgBucketName;
    private String fileBucketName;
    private String videoBucketName;
    private String folder;
}
