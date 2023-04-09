package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class MqConsumerSwitchConfigLoader implements InitializingBean {

    private String mqConsumerSwitchConfig;

    /**
     * group: user-label
     * dataId: mq_consumer_switch_config.json
     */
    public void mqConsumerSwitchConfig(String config) {
        log.info("mq_consumer_switch_config.json, config={}", config);
        this.mqConsumerSwitchConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.mqConsumerSwitchConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.mqConsumerSwitchConfig, e);
        }
        return new JSONObject();
    }

    public boolean tagConsumerSwitchIsOpen(String tag) {
        Objects.requireNonNull(tag, "Null tag.");
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey(tag)) {
            return configJson.getBoolean(tag);
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
