package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hn.onelabel.server.common.utils.LocalDateTimeUtils;
import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleDefineLabel;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleEffectiveTime;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleScript;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class LabelContextRuleMappingConfigLoader implements InitializingBean {

    private String labelContextRuleMappingConfig;

    /**
     * group: user-label
     * dataId: label_context_rule_mapping_config.json
     */
    public void labelContextRuleMappingConfig(String config) {
        log.info("label_context_rule_mapping_config.json, config={}", config);
        this.labelContextRuleMappingConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.labelContextRuleMappingConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.labelContextRuleMappingConfig, e);
        }
        return new JSONObject();
    }

    public List<LabelRule> getLabelRuleList(Long contextId) {
        Objects.requireNonNull(contextId, "Null contextId.");
        List<LabelRule> response = new ArrayList<>();
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey(String.valueOf(contextId))) {
            JSONObject ruleJson = configJson.getJSONObject(String.valueOf(contextId));
            if (ruleJson.containsKey("rules")) {
                JSONArray ruleJsonArray = ruleJson.getJSONArray("rules");
                if (Objects.isNull(ruleJsonArray) || ruleJsonArray.size() == 0) {
                    return response;
                }
                for (int i=0; i<ruleJsonArray.size(); i++) {
                    JSONObject ruleJsonObject = ruleJsonArray.getJSONObject(i);
                    response.add(this.buildLabelRule(contextId, contextId, ruleJsonObject));
                }
            }
        }
        return response;
    }

    private LabelRule buildLabelRule(Long ruleContextId, Long ruleGroupId, JSONObject ruleJsonObject) {
        return new LabelRule(
                ruleContextId,
                ruleGroupId,
                ruleJsonObject.getLong("id"),
                ruleJsonObject.getString("ruleName"),
                ruleJsonObject.getString("ruleDesc"),
                ruleJsonObject.getString("ruleType"),
                new RuleScript(ruleJsonObject.getString("ruleScriptType"), ruleJsonObject.getString("ruleScriptContent")),
                new RuleEffectiveTime(
                        ruleJsonObject.containsKey("ruleEffectiveStartTime") ? LocalDateTimeUtils.date2LocalDateTime(ruleJsonObject.getDate("ruleEffectiveStartTime")) : null,
                        ruleJsonObject.containsKey("ruleEffectiveEndTime") ? LocalDateTimeUtils.date2LocalDateTime(ruleJsonObject.getDate("ruleEffectiveEndTime")) : null
                ),
                new RuleDefineLabel(
                        ruleJsonObject.getString("labelCode"),
                        ruleJsonObject.getLong("labelDimensionKeyId"),
                        ruleJsonObject.getString("labelDimensionKeyDefineType"),
                        ruleJsonObject.getString("labelDimensionFixedKey")
                ),
                ruleJsonObject.containsKey("externalTags") ? ruleJsonObject.getString("externalTags") : null
        );
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
