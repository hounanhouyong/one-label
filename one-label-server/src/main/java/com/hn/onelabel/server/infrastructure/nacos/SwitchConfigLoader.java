package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SwitchConfigLoader implements InitializingBean {

    private String switchConfig;

    /**
     * group: user-label
     * dataId: switch_config.json
     */
    public void switchConfig(String config) {
        log.info("switch_config.json, config={}", config);
        this.switchConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.switchConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.switchConfig, e);
        }
        return new JSONObject();
    }

    public boolean memberCenterMessageListenerSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableMemberCenterMessageListener")) {
            return configJson.getBoolean("enableMemberCenterMessageListener");
        }
        return false;
    }

    public boolean orderCenterMessageListenerSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableOrderCenterMessageListener")) {
            return configJson.getBoolean("enableOrderCenterMessageListener");
        }
        return false;
    }

    public boolean promotionCenterMessageListenerSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enablePromotionCenterMessageListener")) {
            return configJson.getBoolean("enablePromotionCenterMessageListener");
        }
        return false;
    }

    public boolean userLabelMessageListenerSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableUserLabelMessageListener")) {
            return configJson.getBoolean("enableUserLabelMessageListener");
        }
        return false;
    }

    public boolean insertLabelHitRecordsSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableInsertLabelHitRecords")) {
            return configJson.getBoolean("enableInsertLabelHitRecords");
        }
        return false;
    }

    public boolean insertLabelDimensionOperationRecordsSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableInsertLabelDimensionOperationRecords")) {
            return configJson.getBoolean("enableInsertLabelDimensionOperationRecords");
        }
        return false;
    }

    public boolean insertLabelOperationRecordsSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableInsertLabelOperationRecords")) {
            return configJson.getBoolean("enableInsertLabelOperationRecords");
        }
        return false;
    }

    public boolean lockLabelDimensionOperationSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableLockLabelDimensionOperation")) {
            return configJson.getBoolean("enableLockLabelDimensionOperation");
        }
        return false;
    }

    public boolean retryableLabelDimensionOperationSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableRetryableLabelDimensionOperation")) {
            return configJson.getBoolean("enableRetryableLabelDimensionOperation");
        }
        return false;
    }

    public boolean refreshUserLabelSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableRefreshUserLabel")) {
            return configJson.getBoolean("enableRefreshUserLabel");
        }
        return false;
    }

    public boolean clearLabelSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableClearLabel")) {
            return configJson.getBoolean("enableClearLabel");
        }
        return false;
    }

    public boolean readRedisRehashKeySwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableReadRedisRehashKey")) {
            return configJson.getBoolean("enableReadRedisRehashKey");
        }
        return false;
    }

    public boolean searchEsWhenRedisMissesSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableSearchEsWhenRedisMisses")) {
            return configJson.getBoolean("enableSearchEsWhenRedisMisses");
        }
        return false;
    }

    public boolean syncDataToRedisWhereSearchFromEsSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableSyncDataToRedisWhereSearchFromEs")) {
            return configJson.getBoolean("enableSyncDataToRedisWhereSearchFromEs");
        }
        return false;
    }

    public boolean writeUserLabelToEsSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("enableWriteUserLabelToEs")) {
            return configJson.getBoolean("enableWriteUserLabelToEs");
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
