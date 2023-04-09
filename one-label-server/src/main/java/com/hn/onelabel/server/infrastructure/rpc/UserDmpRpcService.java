package com.hn.onelabel.server.infrastructure.rpc;

import com.alibaba.fastjson.JSONObject;
import com.hn.onelabel.server.infrastructure.cache.RedisCacheService;
import com.hn.onelabel.server.infrastructure.nacos.UserDmpQueryConfigLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
public class UserDmpRpcService {

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private UserDmpQueryConfigLoader userDmpQueryConfigLoader;

    public boolean findAttributeValIsTrue(Long userId, String attributeCode) {
        JSONObject jsonObject = this.findUserDmp(userId);
        if (jsonObject.containsKey(attributeCode)) {
            Boolean attributeVal = jsonObject.getBoolean(attributeCode);
            return !Objects.isNull(attributeVal) && attributeVal;
        }
        return false;
    }

    public Date findAttributeVal4Date(Long userId, String attributeCode) {
        JSONObject jsonObject = this.findUserDmp(userId);
        if (jsonObject.containsKey(attributeCode)) {
            return jsonObject.getDate(attributeCode);
        }
        return null;
    }

    public Integer findAttributeVal4Integer(Long userId, String attributeCode) {
        JSONObject jsonObject = this.findUserDmp(userId);
        if (jsonObject.containsKey(attributeCode)) {
            return jsonObject.getInteger(attributeCode);
        }
        return null;
    }

    public Long findAttributeVal4Long(Long userId, String attributeCode) {
        JSONObject jsonObject = this.findUserDmp(userId);
        if (jsonObject.containsKey(attributeCode)) {
            return jsonObject.getLong(attributeCode);
        }
        return null;
    }

    public String findAttributeVal4String(Long userId, String attributeCode) {
        JSONObject jsonObject = this.findUserDmp(userId);
        if (jsonObject.containsKey(attributeCode)) {
            return jsonObject.getString(attributeCode);
        }
        return null;
    }

    public JSONObject findUserDmp(Long userId) {
        Objects.requireNonNull(userId, "Null userId.");
        if (userDmpQueryConfigLoader.useCacheSwitchIsOpen()) {
            return findUserDmpCache(userId);
        } else {
            return findUserDmpNoCache(userId);
        }
    }

    private JSONObject findUserDmpCache(Long userId) {
        return new JSONObject();
    }

    private JSONObject findUserDmpNoCache(Long userId) {
        return new JSONObject();
    }

}
