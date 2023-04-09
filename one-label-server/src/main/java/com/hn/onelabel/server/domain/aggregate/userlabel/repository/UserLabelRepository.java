package com.hn.onelabel.server.domain.aggregate.userlabel.repository;

import com.hn.onelabel.server.domain.aggregate.userlabel.UserLabel;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.BooleanClause;

import java.util.List;

public interface UserLabelRepository {

    boolean findUserHasLabel(Long userId, String labelCode, boolean used);

    List<LabelDimension> findUserLabelDimensions(Long userId, String labelCode, boolean used, boolean syncEsDataToRedis);

    List<LabelDimension> findUserLabelDimensions(Long userId, List<String> labelCodes, boolean used, boolean syncEsDataToRedis);

    LabelDimension findUserLabelDimension(Long userId, String labelCode, Long labelDimensionKeyId, String labelDimensionKey, boolean used);

    UserLabel findUserLabel(Long userId, boolean used, boolean syncEsDataToRedis);

    void saveUserLabelDimensionHotKey(Long userId, String labelCode, Long labelDimensionKeyId, String labelDimensionHotKey, String labelDimensionVal, Long timeoutSeconds);

    void saveUserLabel(UserLabel userLabel);

    void syncUserLabel(Long userId);

    void clearUserLabel(List<Long> userIds);

    Long countLabelDimensions(String labelCode, String dimensionKey);

    Long countLabelDimensions(String labelCode, String dimensionKey, String dimensionVal);

    Pair<List<Long>, Object[]> findUserIdsByConditionsAndPageBySearchAfter(String labelCode, String dimensionKey, String dimensionVal, BooleanClause.Occur occur, Integer pageSize, Object[] sortValues);
}
