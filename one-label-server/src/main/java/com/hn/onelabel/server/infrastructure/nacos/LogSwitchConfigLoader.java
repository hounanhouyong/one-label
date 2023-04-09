package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogSwitchConfigLoader implements InitializingBean {

    private String logSwitchConfig;

    /**
     * group: user-label
     * dataId: log_switch_config.json
     */
    public void logSwitchConfig(String config) {
        log.info("log_switch_config.json, config={}", config);
        this.logSwitchConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.logSwitchConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.logSwitchConfig, e);
        }
        return new JSONObject();
    }

    public boolean redisCacheServicePrintLogSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableRedisCacheServicePrintLog")) {
            return configJson.getBoolean("enableRedisCacheServicePrintLog");
        }
        return true;
    }

    public boolean userLabelRepositoryPrintLogSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableUserLabelRepositoryPrintLog")) {
            return configJson.getBoolean("enableUserLabelRepositoryPrintLog");
        }
        return true;
    }

    public boolean labelDimensionCommandServicePrintLogSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableLabelDimensionCommandServicePrintLog")) {
            return configJson.getBoolean("enableLabelDimensionCommandServicePrintLog");
        }
        return true;
    }

    public boolean labelAdaptationQueryServicePrintLogSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableLabelAdaptationQueryServicePrintLog")) {
            return configJson.getBoolean("enableLabelAdaptationQueryServicePrintLog");
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
