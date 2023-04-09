package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hn.onelabel.api.enums.DatasourceEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class LabelQueryConfigLoader implements InitializingBean {

    private String labelQueryConfig;

    /**
     * group: user-label
     * dataId: label_query_config.json
     */
    public void labelQueryConfig(String config) {
        log.info("label_query_config.json, config={}", config);
        this.labelQueryConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.labelQueryConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.labelQueryConfig, e);
        }
        return new JSONObject();
    }

    private JSONArray getQueryArrangement(String labelCode) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey(labelCode)) {
            JSONObject json = configJson.getJSONObject(labelCode);
            if (json.containsKey("queryArrangement")) {
                return json.getJSONArray("queryArrangement");
            }
        }
        return new JSONArray();
    }

    private JSONObject getQueryArrangement4Datasource(String labelCode, DatasourceEnum datasourceEnum) {
        JSONObject result = new JSONObject();
        JSONArray arrangementJsonArray = this.getQueryArrangement(labelCode);
        for (int i=0; i<arrangementJsonArray.size(); i++) {
            if (datasourceEnum.name().equals(arrangementJsonArray.getJSONObject(i).getString("datasource"))) {
                return arrangementJsonArray.getJSONObject(i);
            }
        }
        return result;
    }

    public List<String> getDatasourceList(String labelCode) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        List<String> list = new ArrayList<>();
        JSONArray arrangementJsonArray = this.getQueryArrangement(labelCode);
        for (int i=0; i<arrangementJsonArray.size(); i++) {
            list.add(arrangementJsonArray.getJSONObject(i).getString("datasource"));
        }
        return list;
    }

    public List<Triple<String, String, String>> getPreloadAttributes(String labelCode, DatasourceEnum datasourceEnum) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(datasourceEnum, "Null datasourceEnum.");
        List<Triple<String, String, String>> response = new ArrayList<>();
        JSONObject json = this.getQueryArrangement4Datasource(labelCode, datasourceEnum);
        if (Objects.isNull(json) || json.size() == 0) {
            return response;
        }
        if (json.containsKey("preload")) {
            JSONArray preloadJsonArray = json.getJSONArray("preload");
            for (int i=0; i<preloadJsonArray.size(); i++) {
                JSONObject attributeJsonObject = preloadJsonArray.getJSONObject(i);
                response.add(Triple.of(attributeJsonObject.getString("attributeLoadFrom"), attributeJsonObject.getString("attributeCode"), attributeJsonObject.getString("attributeType")));
            }
        }
        return response;
    }

    public Triple<Long, String, String> getRule(String labelCode, DatasourceEnum datasourceEnum) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(datasourceEnum, "Null datasourceEnum.");
        JSONObject json = this.getQueryArrangement4Datasource(labelCode, datasourceEnum);
        if (Objects.isNull(json) || json.size() == 0) {
            return null;
        }
        return Triple.of(json.getLong("id"), json.getString("ruleScriptType"), json.getString("ruleScriptContent"));
    }

    public boolean addLabelWhenRuleIsHitSwitchIsOpen(String labelCode, DatasourceEnum datasourceEnum) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(datasourceEnum, "Null datasourceEnum.");
        JSONObject json = this.getQueryArrangement4Datasource(labelCode, datasourceEnum);
        if (Objects.isNull(json) || json.size() == 0) {
            return false;
        }
        if (json.containsKey("addLabelWhenRuleIsHitSwitch")) {
            return json.getBoolean("addLabelWhenRuleIsHitSwitch");
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
