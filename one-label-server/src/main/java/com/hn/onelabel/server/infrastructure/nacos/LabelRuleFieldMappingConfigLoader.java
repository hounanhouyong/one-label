package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class LabelRuleFieldMappingConfigLoader implements InitializingBean {

    private String labelRuleFieldMappingConfig;

    /**
     * group: user-label
     * dataId: label_rule_field_mapping_config.json
     */
    public void labelRuleFieldMappingConfig(String config) {
        log.info("label_rule_field_mapping_config.json, config={}", config);
        this.labelRuleFieldMappingConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.labelRuleFieldMappingConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.labelRuleFieldMappingConfig, e);
        }
        return new JSONObject();
    }

    public Map<String, Pair<String, String>> getFieldMapping(Long contextId) {
        Objects.requireNonNull(contextId, "Null contextId.");
        Map<String, Pair<String, String>> map = new HashMap<>();
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey(String.valueOf(contextId))) {
            JSONObject contextFileMappingJson = configJson.getJSONObject(String.valueOf(contextId));
            if (contextFileMappingJson.containsKey("fieldMapping")) {
                JSONArray jsonArray = contextFileMappingJson.getJSONArray("fieldMapping");
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
        }
        return map;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
