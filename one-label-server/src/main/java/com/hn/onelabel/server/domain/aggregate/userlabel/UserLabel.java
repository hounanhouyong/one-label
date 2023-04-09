package com.hn.onelabel.server.domain.aggregate.userlabel;

import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.Label;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.UserProperties;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UserLabel {
    private Long userId;
    private Date operateTime;
    private List<UserProperties> userProperties;
    private List<Label> userLabels;
    private List<LabelDimension> labelDimensions;

    public UserLabel(Long userId, List<UserProperties> userProperties, List<Label> userLabels, List<LabelDimension> labelDimensions) {
        this.userId = userId;
        this.operateTime = new Date();
        this.userProperties = !CollectionUtils.isEmpty(userProperties) ? userProperties : new ArrayList<>();
        this.userLabels = !CollectionUtils.isEmpty(userLabels) ? userLabels : new ArrayList<>();
        this.labelDimensions = !CollectionUtils.isEmpty(labelDimensions) ? labelDimensions : new ArrayList<>();
    }

    public UserLabel(Long userId, List<Label> userLabels) {
        this.userId = userId;
        this.operateTime = new Date();
        this.userLabels = !CollectionUtils.isEmpty(userLabels) ? userLabels : new ArrayList<>();
        this.userProperties = new ArrayList<>();
        this.labelDimensions = new ArrayList<>();
    }

    public UserLabel(Long userId) {
        this.userId = userId;
        this.operateTime = new Date();
        this.userLabels = new ArrayList<>();
        this.userProperties = new ArrayList<>();
        this.labelDimensions = new ArrayList<>();
    }

}
