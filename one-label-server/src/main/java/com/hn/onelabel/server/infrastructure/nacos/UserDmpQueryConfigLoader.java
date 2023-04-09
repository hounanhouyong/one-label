package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class UserDmpQueryConfigLoader implements InitializingBean {

    private String userDmpQueryConfig;

    /**
     * group: user-label
     * dataId: user_dmp_query_config.json
     */
    public void userDmpQueryConfig(String config) {
        log.info("user_dmp_query_config.json, config={}", config);
        this.userDmpQueryConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.userDmpQueryConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.userDmpQueryConfig, e);
        }
        return new JSONObject();
    }

    public boolean useCacheSwitchIsOpen() {
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("useCacheTimeRangeDaily")) {
            JSONArray jsonArray = configJson.getJSONArray("useCacheTimeRangeDaily");
            if (Objects.isNull(jsonArray) || jsonArray.size() == 0) {
                return false;
            }
            Integer hour = LocalDateTime.now().getHour();
            for (int i=0; i<jsonArray.size(); i++) {
                JSONObject timeRangeJsonObject = jsonArray.getJSONObject(i);
                if (timeRangeJsonObject.containsKey("timeRangeBegin") && timeRangeJsonObject.containsKey("timeRangeEnd")) {
                    Integer timeRangeBegin = timeRangeJsonObject.getInteger("timeRangeBegin");
                    Integer timeRangeEnd = timeRangeJsonObject.getInteger("timeRangeEnd");
                    if (!Objects.isNull(timeRangeBegin) && !Objects.isNull(timeRangeEnd) && timeRangeBegin < hour && timeRangeEnd > hour) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Map<String, Pair<String, String>> getFieldMapping() {
        Map<String, Pair<String, String>> map = new HashMap<>();
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("fieldMapping")) {
            JSONArray jsonArray = configJson.getJSONArray("fieldMapping");
            if (Objects.isNull(jsonArray) || jsonArray.size() == 0) {
                return map;
            }
            for (int i=0; i<jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.containsKey("from") && jsonObject.containsKey("to")) {
                    map.put(jsonObject.getString("from"), Pair.of(jsonObject.getString("to"), jsonObject.getString("fieldType")));
                }
            }
        }
        return map;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
