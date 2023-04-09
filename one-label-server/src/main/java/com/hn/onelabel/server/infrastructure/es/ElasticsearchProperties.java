package com.hn.onelabel.server.infrastructure.es;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {
    private String schema = "http";
    private String ip;
    private int port = 9200;
    private String clusterName;
    private String url;
    private String username;
    private String password;
    private int connectTimeout;
    private int connectRequestTimeout;
    private int socketTimeout;
    private String index;
}
