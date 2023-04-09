package com.hn.onelabel.server.application.rpc;

import com.alibaba.fastjson.JSON;
import com.hn.onelabel.api.common.Result;
import com.hn.onelabel.api.feign.LabelAdminService;
import com.hn.onelabel.api.model.request.DeleteLabelRuleInfoRequest;
import com.hn.onelabel.api.model.request.FindUserLabelsHashFieldStatisticsRequest;
import com.hn.onelabel.api.model.request.SaveLabelRuleInfoRequest;
import com.hn.onelabel.server.infrastructure.cache.RedisCacheService;
import com.hn.onelabel.server.infrastructure.cache.RedisKey;
import com.hn.onelabel.server.service.LabelRuleCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RestController
@Service
public class LabelAdminServiceImpl implements LabelAdminService {

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private LabelRuleCommandService labelRuleCommandService;

    @Override
    public Result<Integer> findUserLabelsHashFieldStatistics(FindUserLabelsHashFieldStatisticsRequest request) {
        Result<Integer> result = new Result<>();
        log.info("[findUserLabelsHashFieldStatistics] - request: {}", JSON.toJSONString(request));
        AtomicInteger total = new AtomicInteger();
        RedisKey.getAllUserLabelsRedisHashKey(request.getStart(), request.getEnd()).forEach(redisKey -> {
            if (redisCacheService.exists(redisKey)) {
                List<Object> list = redisCacheService.hVals(redisKey);
                if (!CollectionUtils.isEmpty(list)) {
                    log.info("[findUserLabelsHashFieldStatistics] - redisKey: {}, total fields: {}", redisKey, list.size());
                    total.addAndGet(list.size());
                }
            }
        });
        return result.success(total.get());
    }

    @Override
    public Result<Boolean> saveLabelRuleInfo(SaveLabelRuleInfoRequest request) {
        Result<Boolean> result = new Result<>();
        log.info("[saveLabelRuleInfo] - request: {}", JSON.toJSONString(request));
        labelRuleCommandService.saveLabelRuleInfo(request);
        return result.success(Boolean.TRUE);
    }

    @Override
    public Result<Boolean> deleteLabelRuleInfo(DeleteLabelRuleInfoRequest request) {
        Result<Boolean> result = new Result<>();
        log.info("[deleteLabelRuleInfo] - request: {}", JSON.toJSONString(request));
        if (!CollectionUtils.isEmpty(request.getRuleIdList())) {
            request.getRuleIdList().forEach(ruleId -> labelRuleCommandService.deleteLabelRuleInfo(ruleId));
        }
        return result.success(Boolean.TRUE);
    }
}
