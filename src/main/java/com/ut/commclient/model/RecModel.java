package com.ut.commclient.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecModel {
    @JSONField(ordinal = 1)
    private String fromType;
    @JSONField(ordinal = 2)
    private String formIp;
    @JSONField(ordinal = 3)
    private int formPort;
    @JSONField(ordinal = 4)
    private String toType;
    @JSONField(ordinal = 5)
    private String toIp;
    @JSONField(ordinal = 6)
    private int toPort;
    @JSONField(ordinal = 7)
    private String content;
}
