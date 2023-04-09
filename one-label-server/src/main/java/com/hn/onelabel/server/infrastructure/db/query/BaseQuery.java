package com.hn.onelabel.server.infrastructure.db.query;

import com.hn.onelabel.server.infrastructure.db.BaseDO;
import lombok.Data;

import java.util.List;

@Data
public class BaseQuery extends BaseDO {
    private Long ltId;
    private Long gtId;
    private String ltCreateTime;
    private String lteCreateTime;
    private String gtCreateTime;
    private String gteCreateTime;
    private String ltUpdateTime;
    private String lteUpdateTime;
    private String gtUpdateTime;
    private String gteUpdateTime;
    private boolean usePaging=false;
    private int offset=0;
    private int rows=200;
    private boolean useSorting=false;
    private List<SortModel> sortModelList;
}
