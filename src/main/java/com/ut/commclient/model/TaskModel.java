package com.ut.commclient.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

//@EqualsAndHashCode(callSuper = true)
@Data
public class TaskModel {
    private String ip;
    private Integer port;
    private String content;
}
