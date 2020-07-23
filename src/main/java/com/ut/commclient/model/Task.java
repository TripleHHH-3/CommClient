package com.ut.commclient.model;

import lombok.Data;

import java.util.List;

@Data
public class Task {
    private List<TaskModel> tcpClientTask;
    private List<TaskModel> tcpServerTask;
    private List<TaskModel> udpDatagramTask;
    private List<TaskModel> udpMulticastTask;
}
