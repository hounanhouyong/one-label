package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class AdaptationAbTestCommonConfigLoader implements InitializingBean {

    private JSONObject configJson;

    /**
     * group: user-label-adapter
     * dataId: adaptation_ab_test_common_config.json
     */
    public void adaptationAbTestCommonConfig(String config) {
        log.info("adaptation_ab_test_common_config.json, config={}", config);
        try {
            configJson = JSONObject.parseObject(config);
            return;
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", config, e);
        }
        configJson = new JSONObject();
    }

    private JSONObject getConfigJson() {
        return this.configJson;
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

    public boolean addAbTestLabelSwitchIsOpen() {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("enableAddAbTestLabelSwitch")) {
            return commonConfig.getBoolean("enableAddAbTestLabelSwitch");
        }
        return false;
    }

    public boolean addAbTestSyncSwitchIsOpen() {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("addAbTestSyncSwitch")) {
            return commonConfig.getBoolean("addAbTestSyncSwitch");
        }
        return false;
    }

    public Integer findAbExperimentGroupMaxUserGroupSize(int defaultVal) {
        JSONObject commonConfig = this.getCommonConfig();
        if (commonConfig.containsKey("findAbExperimentGroupMaxUserGroupSize")) {
            return commonConfig.getInteger("findAbExperimentGroupMaxUserGroupSize");
        }
        return defaultVal;
    }


    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
