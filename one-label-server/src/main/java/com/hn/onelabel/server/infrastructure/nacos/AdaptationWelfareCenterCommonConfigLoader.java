package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class AdaptationWelfareCenterCommonConfigLoader implements InitializingBean {

    private String adaptationWelfareCenterCommonConfig;

    /**
     * group: user-label-adapter
     * dataId: adaptation_welfare_center_common_config.json
     */
    public void adaptationWelfareCenterCommonConfig(String config) {
        log.info("adaptation_welfare_center_common_config.json, config={}", config);
        this.adaptationWelfareCenterCommonConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.adaptationWelfareCenterCommonConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.adaptationWelfareCenterCommonConfig, e);
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

    public boolean addWelfareCenterLabelSwitchIsOpen() {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("enableAddWelfareCenterLabelSwitch")) {
            return commonConfig.getBoolean("enableAddWelfareCenterLabelSwitch");
        }
        return false;
    }

    public boolean addWelfareCenterLabelSyncSwitchIsOpen() {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("addWelfareCenterLabelSyncSwitch")) {
            return commonConfig.getBoolean("addWelfareCenterLabelSyncSwitch");
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
