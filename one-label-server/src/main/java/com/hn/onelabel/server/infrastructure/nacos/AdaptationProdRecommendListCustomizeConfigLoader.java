package com.hn.onelabel.server.infrastructure.nacos;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class AdaptationProdRecommendListCustomizeConfigLoader implements InitializingBean {

    private String adaptationProductRecommendListCustomizeConfig;

    /**
     * group: user-label-adapter
     * dataId: adaptation_product_recommend_list_customize_config.json
     */
    public void adaptationProductRecommendListCustomizeConfig(String config) {
        log.info("adaptation_product_recommend_list_customize_config.json, config={}", config);
        this.adaptationProductRecommendListCustomizeConfig = config;
    }

    private JSONObject getConfigJson() {
        try {
            return JSONObject.parseObject(this.adaptationProductRecommendListCustomizeConfig);
        } catch (Exception e) {
            log.error("JSONObject.parseObject failure, content: {}", this.adaptationProductRecommendListCustomizeConfig, e);
        }
        return new JSONObject();
    }

    public List<String> getTopProductIdList() {
        List<String> topProductIdList = new ArrayList<>();
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("topProductList")) {
            JSONArray jsonArray = configJson.getJSONArray("topProductList");
            if (Objects.isNull(jsonArray) || jsonArray.size() == 0) {
                return topProductIdList;
            }
            for (int i=0; i<jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.containsKey("productId")) {
                    topProductIdList.add(jsonObject.getString("productId"));
                }
            }
        }
        return topProductIdList;
    }

    public List<String> getRemovedProductIdList() {
        List<String> removedProductIdList = new ArrayList<>();
        JSONObject configJson = this.getConfigJson();
        if (configJson.containsKey("removedProductList")) {
            JSONArray jsonArray = configJson.getJSONArray("removedProductList");
            if (Objects.isNull(jsonArray) || jsonArray.size() == 0) {
                return removedProductIdList;
            }
            for (int i=0; i<jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.containsKey("productId")) {
                    removedProductIdList.add(jsonObject.getString("productId"));
                }
            }
        }
        return removedProductIdList;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
