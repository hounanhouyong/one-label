package com.hn.onelabel.server.infrastructure.db.repository.impl;

import com.alibaba.fastjson.JSON;
import com.hn.onelabel.api.enums.LabelDimensionOperationTypeEnum;
import com.hn.onelabel.api.enums.LabelOperationTypeEnum;
import com.hn.onelabel.server.common.utils.LocalDateTimeUtils;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.Label;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import com.hn.onelabel.server.infrastructure.db.LabelDimensionOperationRecordsDO;
import com.hn.onelabel.server.infrastructure.db.LabelOperationRecordsDO;
import com.hn.onelabel.server.infrastructure.db.mapper.LabelDimensionOperationRecordsMapper;
import com.hn.onelabel.server.infrastructure.db.mapper.LabelOperationRecordsMapper;
import com.hn.onelabel.server.infrastructure.nacos.SwitchConfigLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class LabelOperationRecordsRepository {

    @Autowired
    private LabelDimensionOperationRecordsMapper labelDimensionOperationRecordsMapper;
    @Autowired
    private LabelOperationRecordsMapper labelOperationRecordsMapper;

    @Autowired
    private SwitchConfigLoader switchConfigLoader;

    @Async("taskExecutor")
    public void saveLabelDimensionOperationRecords(
            String sequenceId,
            Long userId,
            String labelCode,
            LabelDimensionOperationTypeEnum operationTypeEnum,
            List<LabelDimension> operationData,
            List<LabelDimension> oldAllLabelDimensions,
            List<LabelDimension> newLabelDimensions
    ) {

        if (!switchConfigLoader.insertLabelDimensionOperationRecordsSwitchIsOpen()) {
            return;
        }

        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(operationTypeEnum, "Null labelDimensionOperationType.");

        if (CollectionUtils.isEmpty(operationData)) {
            return;
        }

        List<LabelDimension> oldLabelDimensions = new ArrayList<>();

        if (!CollectionUtils.isEmpty(oldAllLabelDimensions)) {
            oldLabelDimensions = oldAllLabelDimensions.stream().filter(labelDimension -> labelDimension.getLabelCode().equals(labelCode)).collect(Collectors.toList());
        }
        if (Objects.isNull(newLabelDimensions)) {
            newLabelDimensions = new ArrayList<>();
        }

        try {
            LabelDimensionOperationRecordsDO operationRecord = new LabelDimensionOperationRecordsDO();
            operationRecord.setSequenceId(sequenceId);
            operationRecord.setUserId(userId);
            operationRecord.setLabelCode(labelCode);
            operationRecord.setOperationType(operationTypeEnum.name());
            operationRecord.setOperationData(JSON.toJSONString(operationData));
            operationRecord.setOldLabelDimension(JSON.toJSONString(oldLabelDimensions));
            operationRecord.setNewLabelDimension(JSON.toJSONString(newLabelDimensions));
            labelDimensionOperationRecordsMapper.insertEntity(LocalDateTimeUtils.getFormatDateInt(LocalDateTime.now(), "yyyyMM"), operationRecord);
        } catch (Exception e) {
            log.error("[saveLabelDimensionOperationRecords] - insertEntity exception.", e);
        }

    }

    @Async("taskExecutor")
    public void saveLabelOperationRecords(
            String sequenceId,
            Long userId,
            String labelCode,
            LabelOperationTypeEnum operationTypeEnum,
            Label operationData,
            List<Label> oldLabels,
            List<Label> newLabels
    ) {

        if (!switchConfigLoader.insertLabelOperationRecordsSwitchIsOpen()) {
            return;
        }

        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(operationTypeEnum, "Null labelOperationTypeEnum.");

        if (Objects.isNull(operationData)) {
            return;
        }

        if (Objects.isNull(newLabels)) {
            newLabels = new ArrayList<>();
        }

        try {
            LabelOperationRecordsDO operationRecord = new LabelOperationRecordsDO();
            operationRecord.setSequenceId(sequenceId);
            operationRecord.setUserId(userId);
            operationRecord.setLabelCode(labelCode);
            operationRecord.setOperationType(operationTypeEnum.name());
            operationRecord.setOperationData(JSON.toJSONString(operationData));
            operationRecord.setOldLabel(JSON.toJSONString(oldLabels));
            operationRecord.setNewLabel(JSON.toJSONString(newLabels));
            labelOperationRecordsMapper.insertEntity(LocalDateTimeUtils.getFormatDateInt(LocalDateTime.now(), "yyyyMM"), operationRecord);
        } catch (Exception e) {
            log.error("[saveLabelOperationRecords] - insertEntity exception.", e);
        }

    }

}
