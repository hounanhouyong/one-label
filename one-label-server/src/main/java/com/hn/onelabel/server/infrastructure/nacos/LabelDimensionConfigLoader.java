package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hn.onelabel.api.enums.*;
import com.hn.onelabel.api.model.request.LabelDimensionKeyRequest;
import com.hn.onelabel.server.common.utils.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class LabelDimensionConfigLoader implements InitializingBean {

    private String labelDimensionConfig;

    /**
     * group: user-label
     * dataId: label_dimension_config.json
     */
    public void labelDimensionConfig(String config) {
        log.info("label_dimension_config.json, config={}", config);
        this.labelDimensionConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.labelDimensionConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.labelDimensionConfig, e);
        }
        return new JSONObject();
    }

    public List<String> getLabelCodeList() {
        List<String> labelCodeList = new ArrayList<>();
        JSONObject configJson = this.getConfigJson();
        Set<Map.Entry<String, Object>> entries = configJson.entrySet();
        if (CollectionUtils.isEmpty(entries)) {
            return labelCodeList;
        }
        for (Map.Entry<String, Object> entry : entries) {
            labelCodeList.add(entry.getKey());
        }
        return labelCodeList;
    }

    private JSONArray getDimensionDefineJsonArray(String labelCode) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey(labelCode)) {
            JSONObject jsonObject = configJson.getJSONObject(labelCode);
            if (jsonObject.containsKey("dimensionDefine")) {
                return jsonObject.getJSONArray("dimensionDefine");
            }
        }
        return new JSONArray();
    }

    public boolean checkLabelCodeIsValid(String labelCode) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        JSONObject configJson = this.getConfigJson();
        return configJson.containsKey(labelCode);
    }

    public LabelDimensionUpdateStrategyEnum getLabelDimensionUpdateStrategy(String labelCode) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey(labelCode)) {
            JSONObject jsonObject = configJson.getJSONObject(labelCode);
            if (jsonObject.containsKey("updateStrategy")) {
                return LabelDimensionUpdateStrategyEnum.getByName(jsonObject.getString("updateStrategy"));
            }
        }
        return null;
    }

    public boolean updateDimensionKeyIsEnabled(String labelCode, Long dimensionKeyId) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return false;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("enableUpdate")) {
                return jsonObject.getBoolean("enableUpdate");
            }
        }
        return false;
    }

    public boolean invalidDimensionKeyIsEnabled(String labelCode, Long dimensionKeyId) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return false;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("enableInvalid")) {
                return jsonObject.getBoolean("enableInvalid");
            }
        }
        return false;
    }

    public LabelDimensionKeyDefineTypeEnum getDimensionKeyDefineType(String labelCode, Long dimensionKeyId, String dimensionFixedKey) {
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return null;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (!StringUtils.isEmpty(dimensionFixedKey)) {
                if (dimensionFixedKey.equals(jsonObject.getString("dimensionFixedKey"))) {
                    return LabelDimensionKeyDefineTypeEnum.getByName(jsonObject.getString("dimensionKeyDefineType"));
                }
            } else {
                Objects.requireNonNull(dimensionKeyId, "Null dimensionId.");
                if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("dimensionKeyDefineType")) {
                    return LabelDimensionKeyDefineTypeEnum.getByName(jsonObject.getString("dimensionKeyDefineType"));
                }
            }
        }
        return null;
    }

    private Long getDimensionKeyId(String labelCode, String dimensionFixedKey) {
        Assert.isTrue(!StringUtils.isEmpty(dimensionFixedKey), "Null dimensionFixedKey.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return null;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionFixedKey.equals(jsonObject.getString("dimensionFixedKey"))) {
                return jsonObject.getLong("dimensionKeyId");
            }
        }
        return null;
    }

    private String getDimensionDynamicKeyTemplate(String labelCode, Long dimensionKeyId) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return null;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("dimensionDynamicKeyTemplate")) {
                return jsonObject.getString("dimensionDynamicKeyTemplate");
            }
        }
        return null;
    }

    private List<Pair<String, String>> getDimensionDynamicKeyParams(String labelCode, Long dimensionKeyId) {
        List<Pair<String, String>> result = new ArrayList<>();
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return result;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("dimensionDynamicKeyParams")) {
                JSONArray jsonArray = jsonObject.getJSONArray("dimensionDynamicKeyParams");
                for (int j=0; j<jsonArray.size(); j++) {
                    JSONObject json = jsonArray.getJSONObject(j);
                    result.add(Pair.of(json.getString("paramCode"), json.getString("paramType")));
                }
            }
        }
        return result;
    }

    public boolean checkDimensionFixedKeyIsValid(String labelCode, String dimensionFixedKey) {
        Objects.requireNonNull(dimensionFixedKey, "Null dimensionFixedKey.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return false;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (jsonObject.containsKey("dimensionFixedKey") && dimensionFixedKey.equals(jsonObject.getString("dimensionFixedKey"))) {
                return true;
            }
        }
        return false;
    }

    public boolean checkDimensionDynamicKeyIsValid(String labelCode, Long dimensionKeyId, Map<String, String> dataMap) {
        List<Pair<String, String>> list = this.getDimensionDynamicKeyParams(labelCode, dimensionKeyId);
        AtomicBoolean result = new AtomicBoolean(true);
        list.forEach(pair -> {
            if (!result.get()) {
                return;
            }
            if (LabelDimensionDynamicKeyParamTypeEnum.JAVA_LANG.name().equals(pair.getRight())) {
                if (!dataMap.containsKey(pair.getLeft())) {
                    result.set(false);
                }
            }
        });
        return result.get();
    }

    public Pair<Long, String> buildDimensionKeyIdAndDimensionKey(String labelCode, LabelDimensionKeyRequest request) {
        Long dimensionKeyId = this.buildDimensionKeyId(labelCode, request.getDimensionKeyId(), request.getDimensionFixedKey());
        return Pair.of(dimensionKeyId, this.buildDimensionKey(labelCode, dimensionKeyId, request.getDimensionDynamicKeyParams(), request.getDimensionFixedKey()));
    }

    public Long buildDimensionKeyId(String labelCode, Long dimensionKeyId, String dimensionFixedKey) {
        switch (Objects.requireNonNull(this.getDimensionKeyDefineType(labelCode, dimensionKeyId, dimensionFixedKey))) {
            case FIXED_KEY:
                return this.getDimensionKeyId(labelCode, dimensionFixedKey);
            case DYNAMIC_KEY:
                return dimensionKeyId;
        }
        return null;
    }

    private String buildDimensionKey(String labelCode, Long dimensionKeyId, Map<String, String> dataMap, String dimensionFixedKey) {
        switch (Objects.requireNonNull(this.getDimensionKeyDefineType(labelCode, dimensionKeyId, dimensionFixedKey))) {
            case FIXED_KEY:
                Assert.isTrue(!StringUtils.isEmpty(dimensionFixedKey), "Null dimensionFixedKey.");
                return dimensionFixedKey;
            case DYNAMIC_KEY:
                AtomicReference<String> dimensionDynamicKeyTemplate = new AtomicReference<>(this.getDimensionDynamicKeyTemplate(labelCode, dimensionKeyId));
                Objects.requireNonNull(dimensionDynamicKeyTemplate, "Null dimensionDynamicKeyTemplate.");

                List<Pair<String, String>> list = this.getDimensionDynamicKeyParams(labelCode, dimensionKeyId);
                list.forEach(pair -> {
                    switch (Objects.requireNonNull(LabelDimensionDynamicKeyParamTypeEnum.getByName(pair.getRight()))) {
                        case DATE_INT:
                            dimensionDynamicKeyTemplate.set(dimensionDynamicKeyTemplate.get().replaceFirst("%s", LocalDateTimeUtils.getFormatDateString(LocalDateTime.now(), "yyyyMMdd")));
                            break;
                        case JAVA_LANG:
                            if (!Objects.isNull(dataMap)) {
                                dimensionDynamicKeyTemplate.set(dimensionDynamicKeyTemplate.get().replaceFirst("%s", String.valueOf(dataMap.get(pair.getLeft()))));
                            }
                            break;
                    }
                });
                return dimensionDynamicKeyTemplate.get();
        }
        return null;
    }

    public LocalDateTime getInvalidTime(String labelCode, Long dimensionKeyId) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) ||dimensionDefineJsonArray.size() == 0) {
            return null;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("invalidConfig")) {
                JSONObject invalidConfig = jsonObject.getJSONObject("invalidConfig");
                if (this.invalidDimensionKeyIsEnabled(labelCode, dimensionKeyId)) {
                    if (invalidConfig.containsKey("invalidType")) {
                        switch (Objects.requireNonNull(LabelDimensionKeyInvalidTypeEnum.getByName(invalidConfig.getString("invalidType")))) {
                            case ABSOLUTE_TIME:
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                return LocalDateTime.parse(invalidConfig.getString("absoluteInvalidTime"), formatter);
                            case RELATIVE_TIME:
                                Integer num = invalidConfig.getInteger("relativeInvalidTimeNum");
                                switch (Objects.requireNonNull(LabelDimensionKeyRelativeInvalidTimeUnitEnum.getByName(invalidConfig.getString("relativeInvalidTimeUnit")))) {
                                    case D_1:
                                        return LocalDateTime.now().plusDays(num);
                                    case H_1:
                                        return LocalDateTime.now().plusHours(num);
                                    case M_1:
                                        return LocalDateTime.now().plusMinutes(num);
                                    case S_1:
                                        return LocalDateTime.now().plusSeconds(num);
                                }
                                break;
                        }
                    }
                } else {
                    return LocalDateTime.of(2099, 1, 1, 0, 0, 0);
                }
            }
        }
        return null;
    }

    public boolean enableWriteDataToDb(String labelCode, Long dimensionKeyId) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return false;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("enableWriteDataToDb")) {
                return jsonObject.getBoolean("enableWriteDataToDb");
            }
        }
        return false;
    }

    public int getShardingDimensionValPartitionSize(String labelCode, Long dimensionKeyId, int defaultVal) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return defaultVal;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("writeDataToDbConfig")) {
                JSONObject writeDataToDbConfig = jsonObject.getJSONObject("writeDataToDbConfig");
                if (this.enableWriteDataToDb(labelCode, dimensionKeyId)) {
                    if (writeDataToDbConfig.containsKey("shardingDimensionValPartitionSize")) {
                        return writeDataToDbConfig.getInteger("shardingDimensionValPartitionSize");
                    }
                }
            }
        }
        return defaultVal;
    }

    public boolean enableDimensionHotKey(String labelCode, Long dimensionKeyId) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) || dimensionDefineJsonArray.size() == 0) {
            return false;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("enableDimensionHotKey")) {
                return jsonObject.getBoolean("enableDimensionHotKey");
            }
        }
        return false;
    }

    public boolean enableFindLabelDimensionsWhenRedisHotKeyMisses(String labelCode, Long dimensionKeyId) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        JSONArray dimensionDefineJsonArray = this.getDimensionDefineJsonArray(labelCode);
        if (Objects.isNull(dimensionDefineJsonArray) ||dimensionDefineJsonArray.size() == 0) {
            return false;
        }
        for (int i=0; i<dimensionDefineJsonArray.size(); i++) {
            JSONObject jsonObject = dimensionDefineJsonArray.getJSONObject(i);
            if (dimensionKeyId.equals(jsonObject.getLong("dimensionKeyId")) && jsonObject.containsKey("dimensionHotKeyConfig")) {
                JSONObject dimensionHotKeyConfig = jsonObject.getJSONObject("dimensionHotKeyConfig");
                if (this.enableDimensionHotKey(labelCode, dimensionKeyId)) {
                    if (dimensionHotKeyConfig.containsKey("enableFindLabelDimensionsWhenRedisHotKeyMisses")) {
                        return dimensionHotKeyConfig.getBoolean("enableFindLabelDimensionsWhenRedisHotKeyMisses");
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() {

    }
}
