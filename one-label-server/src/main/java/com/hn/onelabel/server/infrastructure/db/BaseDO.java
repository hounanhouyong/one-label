package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;

import java.util.Date;

@Data
public class BaseDO {
    private Long id;
    private Date createTime = new Date();
    private Date updateTime = new Date();
    private Integer deleted = 0;
}
