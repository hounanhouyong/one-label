package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class AdaptationTouchFatigueCommonConfigLoader implements InitializingBean {

    private String adaptationTouchFatigueCommonConfig;

    /**
     * group: user-label-adapter
     * dataId: adaptation_touch_fatigue_common_config.json
     */
    public void adaptationTouchFatigueCommonConfig(String config) {
        log.info("adaptation_touch_fatigue_common_config.json, config={}", config);
        this.adaptationTouchFatigueCommonConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.adaptationTouchFatigueCommonConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.adaptationTouchFatigueCommonConfig, e);
        }
        return new JSONObject();
    }

    private JSONObject getCommonConfig() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("commonConfig")) {
            return configJson.getJSONObject("commonConfig");
        }
        return new JSONObject();
    }

    private JSONObject getLabelConfig() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("labelConfig")) {
            return configJson.getJSONObject("labelConfig");
        }
        return new JSONObject();
    }

    public Long getLabelDimensionKeyId(String labelCode) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        JSONObject labelConfig = this.getLabelConfig();
        if (labelConfig.containsKey(labelCode)) {
            JSONObject jsonObject = labelConfig.getJSONObject(labelCode);
            if (jsonObject.containsKey("labelDimensionKeyId")) {
                return jsonObject.getLong("labelDimensionKeyId");
            }
        }
        return null;
    }

    public boolean touchFatigueIncrSyncSwitchIsOpen() {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("touchFatigueIncreaseSyncSwitch")) {
            return commonConfig.getBoolean("touchFatigueIncreaseSyncSwitch");
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
