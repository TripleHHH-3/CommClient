package com.ut.commclient.model;

import lombok.Data;

@Data
public class TaskModel {
    private String ip;
    private Integer port;
    private String content;
}
