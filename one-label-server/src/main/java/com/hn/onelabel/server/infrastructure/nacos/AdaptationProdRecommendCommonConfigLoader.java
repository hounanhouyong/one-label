package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class AdaptationProdRecommendCommonConfigLoader implements InitializingBean {

    private String adaptationProductRecommendCommonConfig;

    /**
     * group: user-label-adapter
     * dataId: adaptation_product_recommend_common_config.json
     */
    public void adaptationProductRecommendCommonConfig(String config) {
        log.info("adaptation_product_recommend_common_config.json, config={}", config);
        this.adaptationProductRecommendCommonConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.adaptationProductRecommendCommonConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.adaptationProductRecommendCommonConfig, e);
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

    public boolean productRecommendReOrderSyncSwitchIsOpen() {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("productRecommendReOrderSyncSwitch")) {
            return commonConfig.getBoolean("productRecommendReOrderSyncSwitch");
        }
        return false;
    }

    private JSONObject getLabelConfig() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("labelConfig")) {
            return configJson.getJSONObject("labelConfig");
        }
        return new JSONObject();
    }

    public String getCategoryPreferenceDmpLabelCode() {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("categoryPreferenceDmpLabelCode")) {
            return commonConfig.getString("categoryPreferenceDmpLabelCode");
        }
        return null;
    }

    public String getUserLifeCycleDmpLabelCode() {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("userLifeCycleDmpLabelCode")) {
            return commonConfig.getString("userLifeCycleDmpLabelCode");
        }
        return null;
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

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
