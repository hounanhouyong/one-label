package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqTopicContextMappingConfigLoader implements InitializingBean {

    private String mqTopicContextMappingConfig;

    /**
     * group: user-label
     * dataId: mq_topic_context_mapping_config.json
     */
    public void mqTopicContextMappingConfig(String config) {
        log.info("mq_topic_context_mapping_config.json, config={}", config);
        this.mqTopicContextMappingConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.mqTopicContextMappingConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.mqTopicContextMappingConfig, e);
        }
        return new JSONObject();
    }

    public Long getContextId(String topic, String tag) {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey(topic)) {
            if (configJson.getJSONObject(topic).containsKey(tag)) {
                return configJson.getJSONObject(topic).getJSONObject(tag).getLong("contextId");
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
