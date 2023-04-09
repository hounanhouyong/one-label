package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class UserLruConfigLoader implements InitializingBean {

    private String userLruConfig;

    /**
     * group: user-label
     * dataId: user_lru_config.json
     */
    public void userLruConfig(String config) {
        log.info("user_lru_config.json, config={}", config);
        this.userLruConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.userLruConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.userLruConfig, e);
        }
        return new JSONObject();
    }

    public int getHowManyDaysIsUserLruDataRetained(int defaultVal) {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("howManyDaysIsUserLruDataRetained")) {
            return configJson.getInteger("howManyDaysIsUserLruDataRetained");
        }
        return defaultVal;
    }

    public List<String> noClearUserIfTheLabelsExists() {
        List<String> labelCodeList = new ArrayList<>();
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("noClearUserIfTheLabelsExists")) {
            return Arrays.asList(configJson.getString("noClearUserIfTheLabelsExists").split(","));
        }
        return labelCodeList;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
