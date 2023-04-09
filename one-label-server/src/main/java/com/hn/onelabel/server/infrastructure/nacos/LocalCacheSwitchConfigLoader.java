package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalCacheSwitchConfigLoader implements InitializingBean {

    private String localCacheSwitchConfig;

    /**
     * group: user-label
     * dataId: local_cache_switch_config.json
     */
    public void localCacheSwitchConfig(String config) {
        log.info("local_cache_switch_config.json, config={}", config);
        this.localCacheSwitchConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.localCacheSwitchConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.localCacheSwitchConfig, e);
        }
        return new JSONObject();
    }

    public boolean findUserSegmentLoadingCacheSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableFindUserSegmentLoadingCacheSwitch")) {
            return configJson.getBoolean("enableFindUserSegmentLoadingCacheSwitch");
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
