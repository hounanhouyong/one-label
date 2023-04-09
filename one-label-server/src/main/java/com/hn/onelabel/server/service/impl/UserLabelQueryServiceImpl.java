package com.hn.onelabel.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.hn.onelabel.api.enums.DatasourceEnum;
import com.hn.onelabel.api.enums.LabelOperationTypeEnum;
import com.hn.onelabel.api.enums.RuleContextAttributeLoadFromEnum;
import com.hn.onelabel.api.enums.RuleContextAttributeTypeEnum;
import com.hn.onelabel.api.model.request.FindUserHasLabelRequest;
import com.hn.onelabel.api.model.request.FindUserHasLabelsRequest;
import com.hn.onelabel.api.model.request.UserLabelOperationRequest;
import com.hn.onelabel.server.common.utils.GroovyScriptUtil;
import com.hn.onelabel.server.domain.aggregate.userlabel.repository.UserLabelRepository;
import com.hn.onelabel.server.infrastructure.mq.RocketMqProducer;
import com.hn.onelabel.server.infrastructure.nacos.LabelQueryConfigLoader;
import com.hn.onelabel.server.infrastructure.rpc.UserDmpRpcService;
import com.hn.onelabel.server.service.UserLabelQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class UserLabelQueryServiceImpl implements UserLabelQueryService {

    @Autowired
    private UserLabelRepository userLabelRepository;
    @Autowired
    private UserDmpRpcService userDmpRpcService;

    @Autowired
    private LabelQueryConfigLoader labelQueryConfigLoader;

    @Autowired
    private RocketMqProducer mqProducer;

    @Override
    public Boolean userHasLabel(FindUserHasLabelRequest request) {
        AtomicReference<Boolean> result = new AtomicReference<>(false);
        AtomicReference<Boolean> unionQueryRedisAndEs = new AtomicReference<>(false);
        labelQueryConfigLoader.getDatasourceList(request.getLabelCode()).forEach(datasource -> {
            if (result.get()) {
                return;
            }
            switch (Objects.requireNonNull(DatasourceEnum.getByName(datasource))) {
                case REDIS:
                case ES:
                    log.info("[userHasLabel] - query in redis and es, userId: {}, labelCode: {}.", request.getUserId(), request.getLabelCode());
                    if (unionQueryRedisAndEs.get()) {
                        return;
                    }
                    if (userLabelRepository.findUserHasLabel(request.getUserId(), request.getLabelCode(), true)) {
                        result.set(true);
                        return;
                    }
                    unionQueryRedisAndEs.set(true);
                    break;
                case HOLO:
                    log.info("[userHasLabel] - query in holo, userId: {}, labelCode: {}.", request.getUserId(), request.getLabelCode());
                    if (this.queryInHolo(request.getUserId(), request.getLabelCode())) {
                        result.set(true);
                        if (labelQueryConfigLoader.addLabelWhenRuleIsHitSwitchIsOpen(request.getLabelCode(), DatasourceEnum.HOLO)) {
                            Message message = new Message();
                            message.setTopic("xxx");
                            message.setTag("labelOperation");
                            String msgId = mqProducer.syncSend(message, UserLabelOperationRequest.builder()
                                    .userId(request.getUserId())
                                    .labelCode(request.getLabelCode())
                                    .operationType(LabelOperationTypeEnum.ADD_LABEL.name())
                                    .build());
                            log.info("[userHasLabel] - send labelOperation message, msgId: {}.", msgId);
                        }
                        return;
                    }
                    break;
            }
        });
        log.info("[userHasLabel] - query result: {}, request: {}.", result.get(), JSON.toJSONString(request));
        return result.get();
    }

    @Override
    public Boolean userHasLabels(FindUserHasLabelsRequest request) {
        log.info("[userHasLabels] - query: {}.", JSON.toJSONString(request));
        AtomicReference<Boolean> result = new AtomicReference<>(true);
        request.getLabelCodeList().forEach(labelCode -> {
            if (!this.userHasLabel(FindUserHasLabelRequest.builder()
                    .userId(request.getUserId())
                    .labelCode(labelCode)
                    .build())) {
                result.set(false);
            }
        });
        log.info("[userHasLabels] - query result: {}, request: {}.", result.get(), JSON.toJSONString(request));
        return result.get();
    }

    @Override
    public List<String> findUserLabels(FindUserHasLabelsRequest request) {
        log.info("[findUserLabels] - query: {}.", JSON.toJSONString(request));
        List<String> result = new ArrayList<>();
        request.getLabelCodeList().forEach(labelCode -> {
            if (this.userHasLabel(FindUserHasLabelRequest.builder()
                    .userId(request.getUserId())
                    .labelCode(labelCode)
                    .build())) {
                result.add(labelCode);
            }
        });
        log.info("[findUserLabels] query result: {}, request: {}.", result, JSON.toJSONString(request));
        return result;
    }

    private boolean queryInHolo(Long userId, String labelCode) {
        List<Triple<String, String, String>> preloadAttributes = labelQueryConfigLoader.getPreloadAttributes(labelCode, DatasourceEnum.HOLO);
        JSONObject context = new JSONObject();
        if (!CollectionUtils.isEmpty(preloadAttributes)) {
            preloadAttributes.forEach(triple -> {
                if (Objects.requireNonNull(RuleContextAttributeLoadFromEnum.getByName(triple.getLeft())) == RuleContextAttributeLoadFromEnum.LABEL) {
                    log.info("[queryInHolo] - pre load from user label, attribute: {}, labelCode: {}, userId: {}.", triple.getMiddle(), labelCode, userId);
                    context.put(triple.getMiddle(), userLabelRepository.findUserHasLabel(userId, triple.getMiddle(), false));
                }
                if (Objects.requireNonNull(RuleContextAttributeLoadFromEnum.getByName(triple.getLeft())) == RuleContextAttributeLoadFromEnum.DMP) {
                    switch (Objects.requireNonNull(RuleContextAttributeTypeEnum.getByCode(triple.getRight()))) {
                        case BOOLEAN:
                            log.info("[queryInHolo] - pre load from user dmp, attribute: {}, labelCode: {}, userId: {}.", triple.getMiddle(), labelCode, userId);
                            context.put(triple.getMiddle(), userDmpRpcService.findAttributeValIsTrue(userId, triple.getMiddle()));
                            break;
                        case DATE:
                            log.info("[queryInHolo] - pre load from user dmp, attribute: {}, labelCode: {}, userId: {}.", triple.getMiddle(), labelCode, userId);
                            context.put(triple.getMiddle(), userDmpRpcService.findAttributeVal4Date(userId, triple.getMiddle()));
                            break;
                        case INTEGER:
                            log.info("[queryInHolo] - pre load from user dmp, attribute: {}, labelCode: {}, userId: {}.", triple.getMiddle(), labelCode, userId);
                            context.put(triple.getMiddle(), userDmpRpcService.findAttributeVal4Integer(userId, triple.getMiddle()));
                            break;
                        case LONG:
                            log.info("[queryInHolo] - pre load from user dmp, attribute: {}, labelCode: {}, userId: {}.", triple.getMiddle(), labelCode, userId);
                            context.put(triple.getMiddle(), userDmpRpcService.findAttributeVal4Long(userId, triple.getMiddle()));
                            break;
                        case STRING:
                            log.info("[queryInHolo] - pre load from user dmp, attribute: {}, labelCode: {}, userId: {}.", triple.getMiddle(), labelCode, userId);
                            context.put(triple.getMiddle(), userDmpRpcService.findAttributeVal4String(userId, triple.getMiddle()));
                            break;
                    }
                }
            });
            log.info("[queryInHolo] - pre load, attributeList: {}, context: {}, labelCode: {}, userId: {}.", JSON.toJSONString(preloadAttributes), JSON.toJSONString(context), labelCode, userId);
        }

        Triple<Long, String, String> rule = labelQueryConfigLoader.getRule(labelCode, DatasourceEnum.HOLO);

        Object[] args = { context };
        try {
            return (Boolean) Objects.requireNonNull(GroovyScriptUtil.invokeMethod(rule.getRight(), "compute", args));
        } catch (Exception e) {
            log.error("[queryInHolo] - execute groovy script exception, ruleScript: {}", rule.getRight(), e);
        }
        return false;
    }

}
