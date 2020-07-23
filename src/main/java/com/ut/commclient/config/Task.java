package com.ut.commclient.config;

import com.ut.commclient.model.TaskModel;
import lombok.Data;

import java.util.List;

@Data
public class Task {
    private List<TaskModel> tcpClient;
    private List<TaskModel> tcpServer;
    private List<TaskModel> udpDatagram;
    private List<TaskModel> udpMulticast;
}
