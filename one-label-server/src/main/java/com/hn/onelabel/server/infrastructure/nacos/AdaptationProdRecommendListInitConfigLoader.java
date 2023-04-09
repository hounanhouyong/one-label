package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hn.onelabel.adapter.api.enums.RecommendInitListTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class AdaptationProdRecommendListInitConfigLoader implements InitializingBean {

    private String adaptationProductRecommendListInitConfig;

    /**
     * group: user-label-adapter
     * dataId: adaptation_product_recommend_list_init_config.json
     */
    public void adaptationProductRecommendListInitConfig(String config) {
        log.info("adaptation_product_recommend_list_init_config.json, config={}", config);
        this.adaptationProductRecommendListInitConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.adaptationProductRecommendListInitConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.adaptationProductRecommendListInitConfig, e);
        }
        return new JSONObject();
    }

    private JSONArray getConfigJsonArray(RecommendInitListTypeEnum recommendInitListTypeEnum) {
        Objects.requireNonNull(recommendInitListTypeEnum, "Null recommendInitListTypeEnum.");
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey(recommendInitListTypeEnum.name())) {
            return configJson.getJSONArray(recommendInitListTypeEnum.name());
        }
        return new JSONArray();
    }

    public List<Triple<String, String, Integer>> getInitRankingList(RecommendInitListTypeEnum recommendInitListTypeEnum) {
        List<Triple<String, String, Integer>> result = new ArrayList<>();
        JSONArray configJsonArray = this.getConfigJsonArray(recommendInitListTypeEnum);
        if (Objects.isNull(configJsonArray) || configJsonArray.size() == 0) {
            return result;
        }
        for (int i=0; i<configJsonArray.size(); i++) {
            JSONObject jsonObject = configJsonArray.getJSONObject(i);
            result.add(Triple.of(jsonObject.getString("productId"), jsonObject.getString("productCategory"), jsonObject.getInteger("rankingNo")));
        }
        return result;
    }

    public List<Triple<String, String, Integer>> getInitRankingList(Integer userLifeCycle) {
        RecommendInitListTypeEnum recommendInitListTypeEnum = !Objects.isNull(userLifeCycle) && userLifeCycle == 1 ? RecommendInitListTypeEnum.USER_IN_THE_INTRODUCTION_PERIOD : RecommendInitListTypeEnum.DEFAULT;
        return this.getInitRankingList(recommendInitListTypeEnum);
    }

    public String getCategoryByProductId(Integer userLifeCycle, String productId) {
        Objects.requireNonNull(productId, "Null productId.");
        RecommendInitListTypeEnum recommendInitListTypeEnum = !Objects.isNull(userLifeCycle) && userLifeCycle == 1 ? RecommendInitListTypeEnum.USER_IN_THE_INTRODUCTION_PERIOD : RecommendInitListTypeEnum.DEFAULT;
        JSONArray configJsonArray = this.getConfigJsonArray(recommendInitListTypeEnum);
        if (Objects.isNull(configJsonArray) || configJsonArray.size() == 0) {
            return null;
        }
        for (int i=0; i<configJsonArray.size(); i++) {
            JSONObject jsonObject = configJsonArray.getJSONObject(i);
            if (jsonObject.containsKey("productId") && jsonObject.getString("productId").equals(productId)) {
                return jsonObject.getString("productCategory");
            }
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
