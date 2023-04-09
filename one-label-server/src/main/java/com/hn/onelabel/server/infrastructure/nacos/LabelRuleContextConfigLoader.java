package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Slf4j
@Component
public class LabelRuleContextConfigLoader implements InitializingBean {

    private String labelRuleContextConfig;

    /**
     * group: user-label
     * dataId: label_rule_context_config.json
     */
    public void labelRuleContextConfig(String config) {
        log.info("label_rule_context_config.json, config={}", config);
        this.labelRuleContextConfig = config;
    }

    private JSONArray getConfigJsonArray() {
        try {
            return JSONObject.parseArray(this.labelRuleContextConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseArray failure, content: {}", this.labelRuleContextConfig, e);
        }
        return new JSONArray();
    }

    public JSONObject getContextAttributes(Long contextId) {
        Objects.requireNonNull(contextId, "Null contextId.");
        JSONArray configJsonArray = this.getConfigJsonArray();
        if (Objects.isNull(configJsonArray)) {
            return new JSONObject();
        }
        for (int i=0; i<configJsonArray.size(); i++) {
            if (contextId.equals(configJsonArray.getJSONObject(i).getLong("contextId"))) {
                return configJsonArray.getJSONObject(i);
            }
        }
        return new JSONObject();
    }

    public List<Pair<String, String>> getContextAttributeCodeAndType(Long contextId) {
        Objects.requireNonNull(contextId, "Null contextId.");
        List<Pair<String, String>> response = new ArrayList<>();
        JSONObject contextAttributeJson = this.getContextAttributes(contextId);
        if (Objects.isNull(contextAttributeJson) || contextAttributeJson.size() == 0) {
            return response;
        }
        if (contextAttributeJson.containsKey("contextAttributes")) {
            JSONArray contextAttributeJsonArray = contextAttributeJson.getJSONArray("contextAttributes");
            for (int i=0; i<contextAttributeJsonArray.size(); i++) {
                JSONObject contextAttributeJsonObject = contextAttributeJsonArray.getJSONObject(i);
                response.add(Pair.of(contextAttributeJsonObject.getString("attributeCode"), contextAttributeJsonObject.getString("attributeType")));
            }
        }
        return response;
    }

    public List<Triple<String, String, String>> getContextAttributes(Long contextId, String attributeLoadType) {
        Objects.requireNonNull(attributeLoadType, "Null attributeLoadType.");
        List<Triple<String, String, String>> response = new ArrayList<>();
        JSONObject contextAttributeJson = this.getContextAttributes(contextId);
        if (Objects.isNull(contextAttributeJson) || contextAttributeJson.size() == 0) {
            return response;
        }
        if (contextAttributeJson.containsKey("contextAttributes")) {
            JSONArray contextAttributeJsonArray = contextAttributeJson.getJSONArray("contextAttributes");
            for (int i=0; i<contextAttributeJsonArray.size(); i++) {
                JSONObject contextAttributeJsonObject = contextAttributeJsonArray.getJSONObject(i);
                if (contextAttributeJsonObject.containsKey("attributeLoadType") && contextAttributeJsonObject.getString("attributeLoadType").equals(attributeLoadType)) {
                    response.add(Triple.of(contextAttributeJsonObject.getString("attributeLoadFrom"), contextAttributeJsonObject.getString("attributeCode"), contextAttributeJsonObject.getString("attributeType")));
                }
            }
        }
        return response;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
