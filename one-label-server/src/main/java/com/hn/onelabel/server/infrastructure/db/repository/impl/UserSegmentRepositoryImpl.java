package com.hn.onelabel.server.infrastructure.db.repository.impl;

import com.hn.onelabel.api.enums.RuleStatusEnum;
import com.hn.onelabel.api.enums.UserSegmentTypeEnum;
import com.hn.onelabel.server.domain.aggregate.usersegment.UserSegment;
import com.hn.onelabel.server.domain.aggregate.usersegment.repository.UserSegmentRepository;
import com.hn.onelabel.server.infrastructure.cache.LocalCacheService;
import com.hn.onelabel.server.infrastructure.db.UserAbExperimentInfoDO;
import com.hn.onelabel.server.infrastructure.db.UserSegmentRuleInfoDO;
import com.hn.onelabel.server.infrastructure.db.converter.UserAbExperimentInfoDOConverter;
import com.hn.onelabel.server.infrastructure.db.converter.UserSegmentRuleInfoDOConverter;
import com.hn.onelabel.server.infrastructure.db.mapper.UserAbExperimentInfoMapper;
import com.hn.onelabel.server.infrastructure.db.mapper.UserSegmentRuleInfoMapper;
import com.hn.onelabel.server.infrastructure.db.query.UserSegmentRuleInfoQuery;
import com.hn.onelabel.server.infrastructure.nacos.LocalCacheSwitchConfigLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserSegmentRepositoryImpl implements UserSegmentRepository {

    @Autowired
    private UserSegmentRuleInfoMapper userSegmentRuleInfoMapper;
    @Autowired
    private UserAbExperimentInfoMapper userAbExperimentInfoMapper;

    @Autowired
    private LocalCacheService localCacheService;

    @Autowired
    private LocalCacheSwitchConfigLoader localCacheSwitchConfigLoader;

    @Override
    public void saveUserSegment(UserSegment userSegment, String creator) {
        try {
            UserSegmentRuleInfoDO entity = UserSegmentRuleInfoDOConverter.INSTANCE.from(userSegment);
            entity.setStatus(RuleStatusEnum.ENABLED.name());
            entity.setCreator(creator);
            entity.setModifier(creator);
            userSegmentRuleInfoMapper.insertEntity(entity);
            userAbExperimentInfoMapper.batchInsert(userSegment.getAbExperimentList().stream().map(abExperiment -> {
                UserAbExperimentInfoDO abEntity = UserAbExperimentInfoDOConverter.INSTANCE.from(entity.getId(), abExperiment);
                abEntity.setCreator(creator);
                abEntity.setModifier(creator);
                return abEntity;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("[saveUserSegment] - insertEntity exception.", e);
        }
    }

    @Override
    public void disableUserSegment(List<Long> segmentIds) {
        if (CollectionUtils.isEmpty(segmentIds)) {
            return;
        }
        segmentIds.forEach(segmentId -> {
            UserSegmentRuleInfoDO entity = new UserSegmentRuleInfoDO();
            entity.setId(segmentId);
            entity.setStatus(RuleStatusEnum.DISABLED.name());
            entity.setUpdateTime(new Date());
            userSegmentRuleInfoMapper.updateByPrimaryKey(entity);
            if (localCacheSwitchConfigLoader.findUserSegmentLoadingCacheSwitchIsOpen()) {
                // invalid
                UserSegmentRuleInfoDO userSegmentRuleInfo = userSegmentRuleInfoMapper.selectByPrimaryKey(segmentId);
                if (!Objects.isNull(userSegmentRuleInfo) && UserSegmentTypeEnum.PROMOTION_PLAN.name().equals(userSegmentRuleInfo.getSegmentType())) {
                    localCacheService.invalidUserSegmentRuleCache4PromotionPlan(userSegmentRuleInfo.getSegmentType());
                }
                // invalid
                localCacheService.invalidUserAbExperimentCache(segmentId);
            }
        });
    }

    @Override
    public List<UserSegment> findUserSegments(UserSegmentTypeEnum segmentTypeEnum, RuleStatusEnum statusEnum, List<String> externalExperimentGroupIdList) {
        if (CollectionUtils.isEmpty(externalExperimentGroupIdList)) {
            return new ArrayList<>();
        }
        List<UserSegmentRuleInfoDO> segmentRuleInfoList;
        if (localCacheSwitchConfigLoader.findUserSegmentLoadingCacheSwitchIsOpen() && RuleStatusEnum.ENABLED.name().equals(statusEnum.name()) && UserSegmentTypeEnum.PROMOTION_PLAN.name().equals(segmentTypeEnum.name())) {
            segmentRuleInfoList = localCacheService.findUserSegmentRules4PromotionPlan(externalExperimentGroupIdList);
        } else {
            UserSegmentRuleInfoQuery dynamicQuery = new UserSegmentRuleInfoQuery();
            dynamicQuery.setSegmentType(segmentTypeEnum.name());
            dynamicQuery.setStatus(statusEnum.name());
            dynamicQuery.setExternalExperimentGroupIdList(externalExperimentGroupIdList);
            segmentRuleInfoList = userSegmentRuleInfoMapper.selectByCondition(dynamicQuery);
        }
        if (CollectionUtils.isEmpty(segmentRuleInfoList)) {
            return new ArrayList<>();
        }
        return this.buildUserSegmentList(segmentRuleInfoList);
    }

    @Override
    public UserSegment findUserSegment(UserSegmentTypeEnum segmentTypeEnum, RuleStatusEnum statusEnum, String externalExperimentGroupId) {
        if (Objects.isNull(externalExperimentGroupId)) {
            return null;
        }
        List<UserSegmentRuleInfoDO> segmentRuleInfoList;
        if (localCacheSwitchConfigLoader.findUserSegmentLoadingCacheSwitchIsOpen() && RuleStatusEnum.ENABLED.name().equals(statusEnum.name()) && UserSegmentTypeEnum.PROMOTION_PLAN.name().equals(segmentTypeEnum.name())) {
            segmentRuleInfoList = localCacheService.findUserSegmentRules4PromotionPlan(Collections.singletonList(externalExperimentGroupId));
        } else {
            UserSegmentRuleInfoQuery dynamicQuery = new UserSegmentRuleInfoQuery();
            dynamicQuery.setSegmentType(segmentTypeEnum.name());
            dynamicQuery.setStatus(statusEnum.name());
            dynamicQuery.setExternalExperimentGroupId(externalExperimentGroupId);
            segmentRuleInfoList = userSegmentRuleInfoMapper.selectByCondition(dynamicQuery);
        }
        if (CollectionUtils.isEmpty(segmentRuleInfoList)) {
            return null;
        }
        return this.buildUserSegment(segmentRuleInfoList.get(0));
    }

    private List<UserSegment> buildUserSegmentList(List<UserSegmentRuleInfoDO> segmentRuleInfoList) {
        if (CollectionUtils.isEmpty(segmentRuleInfoList)) {
            return new ArrayList<>();
        }
        return segmentRuleInfoList.stream().map(segmentRuleInfo -> {
            UserSegment userSegment = UserSegmentRuleInfoDOConverter.INSTANCE.to(segmentRuleInfo);
            userSegment.setAbExperimentList(UserAbExperimentInfoDOConverter.INSTANCE.to(localCacheService.findUserAbExperimentsBySegmentRuleId(segmentRuleInfo.getId())));
            return userSegment;
        }).collect(Collectors.toList());
    }

    private UserSegment buildUserSegment(UserSegmentRuleInfoDO segmentRuleInfo) {
        if (Objects.isNull(segmentRuleInfo)) {
            return null;
        }
        UserSegment userSegment = UserSegmentRuleInfoDOConverter.INSTANCE.to(segmentRuleInfo);
        userSegment.setAbExperimentList(UserAbExperimentInfoDOConverter.INSTANCE.to(localCacheService.findUserAbExperimentsBySegmentRuleId(segmentRuleInfo.getId())));
        return userSegment;
    }

}
