package com.hn.onelabel.server.common.component;

import com.hn.onelabel.server.common.function.CacheSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class CacheService {

    /**
     * @param cacheSelector
     * @param databaseSelector
     * @return T
     */
    public static <T> T selectCacheByTemplate(CacheSelector<T> cacheSelector, Supplier<T> databaseSelector) {
        try {
            log.debug("query data from redis ······");
            T t = cacheSelector.select();
            if (t == null) {
                return databaseSelector.get();
            } else {
                return t;
            }
        } catch (Exception e) {
            log.error("redis error: ", e);
            log.debug("query data from database ······");
            return databaseSelector.get();
        }
    }
}
