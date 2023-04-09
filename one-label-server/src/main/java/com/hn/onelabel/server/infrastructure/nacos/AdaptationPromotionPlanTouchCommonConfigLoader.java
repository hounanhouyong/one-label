package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class AdaptationPromotionPlanTouchCommonConfigLoader implements InitializingBean {

    private String adaptationPromotionPlanTouchCommonConfig;

    /**
     * group: user-label-adapter
     * dataId: adaptation_promotion_plan_touch_common_config.json
     */
    public void adaptationPromotionPlanTouchCommonConfig(String config) {
        log.info("adaptation_promotion_plan_touch_common_config.json, config={}", config);
        this.adaptationPromotionPlanTouchCommonConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.adaptationPromotionPlanTouchCommonConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.adaptationPromotionPlanTouchCommonConfig, e);
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

    public boolean reportPromotionPlanTouchResultSyncSwitchIsOpen() {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("reportPromotionPlanTouchResultSyncSwitch")) {
            return commonConfig.getBoolean("reportPromotionPlanTouchResultSyncSwitch");
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
