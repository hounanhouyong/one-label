package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ParallelProcessingSwitchConfigLoader implements InitializingBean {

    private String parallelProcessingSwitchConfig;

    /**
     * group: user-label
     * dataId: parallel_processing_switch_config.json
     */
    public void parallelProcessingSwitchConfig(String config) {
        log.info("parallel_processing_switch_config.json, config={}", config);
        this.parallelProcessingSwitchConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.parallelProcessingSwitchConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.parallelProcessingSwitchConfig, e);
        }
        return new JSONObject();
    }

    public boolean parallelBuildUserLabelHistorySwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableParallelBuildUserLabelHistory")) {
            return configJson.getBoolean("enableParallelBuildUserLabelHistory");
        }
        return false;
    }

    public boolean parallelWriteLabelDimensionsToDbSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableParallelWriteLabelDimensionsToDb")) {
            return configJson.getBoolean("enableParallelWriteLabelDimensionsToDb");
        }
        return false;
    }

    public boolean parallelFindLabelDimensionsSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableParallelFindLabelDimensions")) {
            return configJson.getBoolean("enableParallelFindLabelDimensions");
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
