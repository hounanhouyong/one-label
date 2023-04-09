package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class MqProducerSwitchConfigLoader implements InitializingBean {

    private String mqProducerSwitchConfig;

    /**
     * group: user-label
     * dataId: mq_producer_switch_config.json
     */
    public void mqProducerSwitchConfig(String config) {
        log.info("mq_producer_switch_config.json, config={}", config);
        this.mqProducerSwitchConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.mqProducerSwitchConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.mqProducerSwitchConfig, e);
        }
        return new JSONObject();
    }

    public boolean tagProducerSwitchIsOpen(String tag) {
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
