package com.hn.onelabel.server.service;

import com.hn.onelabel.api.model.request.LabelDimensionRequest;
import org.apache.commons.lang3.tuple.Triple;

import com.hn.onelabel.api.model.request.LabelDimensionKeyRequest;

import java.time.LocalDateTime;
import java.util.List;

public interface LabelDimensionCommandService {

    void labelDimensionOperation(String sequenceId, Long userId, String labelCode, List<LabelDimensionRequest> labelDimensionRequestList);

    void labelDimensionIncr(String sequenceId, Long userId, String labelCode, LabelDimensionKeyRequest labelDimensionKeyRequest, Integer increaseVal);

    void clearLabelDimensionInvalidData(LocalDateTime invalidDate, long stepSize);

    void deleteLabelDimensionOperation(String sequenceId, String labelCode, List<Triple<Long, Long, String>> userDimensionKeyList);

    void deleteLabelDimensionOperation(String sequenceId, Long userId, String labelCode, List<String> dimensionKeyList);

}
