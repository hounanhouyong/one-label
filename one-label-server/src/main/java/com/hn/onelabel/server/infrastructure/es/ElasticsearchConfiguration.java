package com.hn.onelabel.server.infrastructure.es;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.BiConsumer;

@Slf4j
@ConditionalOnProperty(value = "elasticsearch.enabled", matchIfMissing = true)
@EnableConfigurationProperties(ElasticsearchProperties.class)
@Configuration
public class ElasticsearchConfiguration {

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    private RestHighLevelClient client;

    @Bean
    public RestHighLevelClient esClient(){
        if(StringUtils.isEmpty(elasticsearchProperties.getUsername())){
            return new RestHighLevelClient(RestClient.builder(HttpHost.create(elasticsearchProperties.getUrl())));
        }
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(elasticsearchProperties.getUsername(), elasticsearchProperties.getPassword()));
        this.client = new RestHighLevelClient(
                RestClient.builder(HttpHost.create(elasticsearchProperties.getUrl()))
                        .setHttpClientConfigCallback(httpAsyncClientBuilder -> {
                            httpAsyncClientBuilder.disableAuthCaching();
                            return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                        })
        );
        return this.client;
    }

    @Bean
    public BulkProcessor bulkProcessor() {

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                log.info("[bulkProcessor] - beforeBulk, executionId: {}, request: {}.", executionId, JSON.toJSONString(request));
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                log.info("[bulkProcessor] - afterBulk, executionId: {}, request: {}, response: {}.", executionId, JSON.toJSONString(request), JSON.toJSONString(response));
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                log.info("[bulkProcessor] - afterBulk, executionId: {}, request: {}, errorMessage: {}.", executionId, JSON.toJSONString(request), failure.getMessage());
            }
        };

        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer = (request, bulkListener) -> this.client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener);

        return BulkProcessor.builder(bulkConsumer, listener)
                .setBulkActions(1000)
                .setBulkSize(new ByteSizeValue(1, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .setConcurrentRequests(1)
                .setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                .build();
    }

}
