package com.ut.commclient.thread;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.ut.commclient.component.UdpDatagramTab;
import com.ut.commclient.component.UdpMulticastTab;
import com.ut.commclient.config.Config;
import com.ut.commclient.model.TaskModel;
import com.ut.commclient.component.TcpClientTab;
import com.ut.commclient.component.TcpServerTab;
import com.ut.commclient.model.Task;
import com.ut.commclient.util.ListUtil;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import java.io.FileReader;
import java.util.List;

public class TaskThread implements Runnable {

    private final TabPane tcpClientTabPane;
    private final TabPane tcpServerTabPane;
    private final TabPane udpDatagramTabPane;
    private final TabPane udpMulticastTabPane;

    public TaskThread(StackPane stackPane) {
        ObservableList<Node> children = stackPane.getChildren();
        tcpClientTabPane = (TabPane) children.get(0);
        tcpServerTabPane = (TabPane) children.get(1);
        udpDatagramTabPane = (TabPane) children.get(2);
        udpMulticastTabPane = (TabPane) children.get(3);
    }

    @Override
    public void run() {
        while (true) {
            //任务间隔时间
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //读取文件，出错则继续等待
            Task task;
            try {
                YamlReader reader = new YamlReader(new FileReader(Config.taskPath));
//                YamlReader reader = new YamlReader(new FileReader(".\\task\\task.yml"));
                task = JSON.parseObject(JSON.toJSONString(reader.read()), Task.class);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            if (task == null) continue;

            List<TaskModel> tcpClientTask = task.getTcpClientTask();
            if (ListUtil.GtZero(tcpClientTask)) {

                out:
                for (TaskModel taskModel : tcpClientTask) {

                    //遍历查看是否存在已连接的任务目的host，存在则直接使用writer发送消息，并且跳出到外层循环
                    for (Tab tab : tcpClientTabPane.getTabs()) {
                        TcpClientTab tcpClientTab = (TcpClientTab) tab;
                        if (tcpClientTab.getIpTxt().getText().equals(taskModel.getIp()) &&
                                tcpClientTab.getPortTxt().getText().equals(taskModel.getPort().toString()) &&
                                tcpClientTab.getBeginBtn().isDisabled()) {
                            tcpClientTab.getSendMsgTxt().setText(taskModel.getContent());
                            tcpClientTab.sendMsg();
                            continue out;
                        }
                    }

                    //当不存在已连接任务目标，则创建tab和socket并发送信息
                    TcpClientTab tcpClientTab = new TcpClientTab();
                    tcpClientTab.setText(taskModel.getIp() + ":" + taskModel.getPort());
                    Platform.runLater(() -> tcpClientTabPane.getTabs().add(tcpClientTab));
                    tcpClientTab.getIpTxt().setText(taskModel.getIp());
                    tcpClientTab.getPortTxt().setText(taskModel.getPort().toString());
                    tcpClientTab.getBeginBtn().fire();
                    boolean isSend = false;
                    while (!isSend) {
                        if (tcpClientTab.getSocket() != null && tcpClientTab.getSocket().isConnected() && tcpClientTab.getWriter() != null) {
                            tcpClientTab.getSendMsgTxt().setText(taskModel.getContent());
                            tcpClientTab.getSendBtn().fire();
                            isSend = true;
                        }
                    }

                }
            }

            //tcpServerTask任务
            List<TaskModel> tcpServerTask = task.getTcpServerTask();
            if (ListUtil.GtZero(tcpServerTask)) {
                tcpServerTask.forEach(taskModel -> {

                    tcpServerTabPane.getTabs().forEach(tab -> {
                        ((TcpServerTab) tab).getClientListView().getItems().forEach(clientModel -> {
                            if (clientModel.getIp().equals(taskModel.getIp()) &&
                                    clientModel.getPort().equals(taskModel.getPort()) &&
                                    clientModel.getSocket().isConnected()) {
                                clientModel.getWriter().writeFlush(taskModel.getContent());
                            }
                        });
                    });

                });
            }

            //udpDatagramTask任务
            List<TaskModel> udpDatagramTask = task.getUdpDatagramTask();
            if (ListUtil.GtZero(udpDatagramTask)) {
                UdpDatagramTab udpDatagramTab = new UdpDatagramTab();
                udpDatagramTab.setText("New Tab");
                Platform.runLater(() -> udpDatagramTabPane.getTabs().add(udpDatagramTab));
                udpDatagramTask.forEach(taskModel -> {
                    udpDatagramTab.getIpTxt().setText(taskModel.getIp());
                    udpDatagramTab.getSendPortTxt().setText(taskModel.getPort().toString());
                    udpDatagramTab.getSendMsgTxt().setText(taskModel.getContent());
                    udpDatagramTab.getBindBtn().fire();
                    udpDatagramTab.getSendBtn().fire();
                });
            }

            //udpMulticastTask任务
            List<TaskModel> udpMulticastTask = task.getUdpMulticastTask();
            if (ListUtil.GtZero(udpMulticastTask)) {
                UdpMulticastTab udpMulticastTab = new UdpMulticastTab();
                udpMulticastTab.setText("New Tab");
                Platform.runLater(() -> udpMulticastTabPane.getTabs().add(udpMulticastTab));
                udpMulticastTask.forEach(taskModel -> {
                    udpMulticastTab.getBindIpGroupTxt().setText(taskModel.getIp());
                    udpMulticastTab.getBindPortTxt().setText(taskModel.getPort().toString());
                    udpMulticastTab.getSendMsgTxt().setText(taskModel.getContent());
                    udpMulticastTab.getBindBeginBtn().fire();
                    udpMulticastTab.getSendBtn().fire();
                });
            }

        }
    }
}


//    TcpClientTab tcpClientTab = new TcpClientTab();
//                    tcpClientTab.setText(taskModel.getIp() + ":" + taskModel.getPort());
//                            tcpClientTabPane.getTabs().add(tcpClientTab);
//                            tcpClientTab.getIpTxt().setText(taskModel.getIp());
//                            tcpClientTab.getPortTxt().setText(taskModel.getPort().toString());
//                            tcpClientTab.getBeginBtn().fire();
//                            boolean isSend = false;
//                            while (!isSend) {
//                            if (tcpClientTab.getSocket().isConnected()) {
//                            tcpClientTab.getSendMsgTxt().setText(taskModel.getContent());
//                            tcpClientTab.getSendBtn().fire();
//                            isSend = true;
//                            }
//                            }