package com.hn.onelabel.server.service;

import com.alibaba.fastjson.JSONObject;
import com.hn.onelabel.api.enums.LabelOperationTypeEnum;
import com.hn.onelabel.api.model.request.HitLabelRulesOperationRequest;

import java.util.List;

public interface UserLabelCommandService {

    boolean labelOperation(String sequenceId, JSONObject context, Long ruleContextId);

    boolean labelOperation(String sequenceId, Long userId, LabelOperationTypeEnum labelOperationTypeEnum, String labelCode, boolean used);

    boolean hitLabelRulesOperation(String sequenceId, HitLabelRulesOperationRequest request);

    void clear(List<Long> userIds);

    void clear(String labelCode, int start, int end);

    void clearLru(int start, int end);

    void refreshUserLabelsData(int start, int end);

    void refreshUserLabel(String sequenceId, Long userId);

    void syncUserLabel(String sequenceId, Long userId);
}
