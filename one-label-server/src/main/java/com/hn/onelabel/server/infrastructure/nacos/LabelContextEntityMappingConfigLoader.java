package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class LabelContextEntityMappingConfigLoader implements InitializingBean {

    private String labelContextEntityMappingConfig;

    /**
     * group: user-label
     * dataId: label_context_entity_mapping_config.json
     */
    public void labelContextEntityMappingConfig(String config) {
        log.info("label_context_entity_mapping_config.json, config={}", config);
        this.labelContextEntityMappingConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.labelContextEntityMappingConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.labelContextEntityMappingConfig, e);
        }
        return new JSONObject();
    }

    public Long getEntityId(Long contextId, JSONObject context) {
        Objects.requireNonNull(contextId, "Null contextId");
        Objects.requireNonNull(context, "Null context.");
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey(String.valueOf(contextId))) {
            JSONObject mappingJson = configJson.getJSONObject(String.valueOf(contextId));
            if (mappingJson.containsKey("entityId")) {
                String mapping = mappingJson.getString("entityId");
                if (mapping.contains("->")) {
                    String[] mappingArray = mapping.split("->");
                    for (int i=0; i< mappingArray.length; i++) {
                        if (context.containsKey(mappingArray[i])) {
                            if (i == mappingArray.length - 1) {
                                return context.getLong(mappingArray[i]);
                            }
                            context = context.getJSONObject(mappingArray[i]);
                        } else {
                            return null;
                        }
                    }
                } else if (context.containsKey(mapping)) {
                    return context.getLong(mapping);
                }
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
