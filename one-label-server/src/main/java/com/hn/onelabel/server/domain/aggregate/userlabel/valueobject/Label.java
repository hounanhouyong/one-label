package com.hn.onelabel.server.domain.aggregate.userlabel.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Label {
    private String primaryClassification;
    private String secondaryClassification;
    private String labelGroupCode;
    private String labelCode;
    private Date createTime;
    private Date invalidTime;
}
