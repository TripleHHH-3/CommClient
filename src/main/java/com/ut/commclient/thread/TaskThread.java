package com.ut.commclient.thread;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.ut.commclient.model.TaskModel;
import com.ut.commclient.component.TcpClientTab;
import com.ut.commclient.component.TcpServerTab;
import com.ut.commclient.config.Task;
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
    private TabPane udpDatagramTabPane;
    private TabPane udpMulticastTabPane;

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
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Task task;
            try {
//                YamlReader reader = new YamlReader(new FileReader("src\\main\\resources\\task\\task.yml"));
                YamlReader reader = new YamlReader(new FileReader(".\\task\\task.yml"));
                task = JSON.parseObject(JSON.toJSONString(reader.read()), Task.class);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if (task == null) continue;

            List<TaskModel> tcpClient = task.getTcpClient();
            if (ListUtil.GtZero(tcpClient)) {

                out:
                for (TaskModel taskModel : tcpClient) {

                    synchronized (tcpClientTabPane) {
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
                    }
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


                List<TaskModel> tcpServer = task.getTcpServer();
                if (ListUtil.GtZero(tcpServer)) {
                    tcpServer.forEach(taskModel -> {
                        synchronized (tcpServerTabPane) {

                            tcpServerTabPane.getTabs().forEach(tab -> {
                                ((TcpServerTab) tab).getClientListView().getItems().forEach(clientModel -> {
                                    if (clientModel.getIp().equals(taskModel.getIp()) &&
                                            clientModel.getPort().equals(taskModel.getPort())) {
                                        clientModel.getWriter().writeFlush(taskModel.getContent());
                                    }
                                });
                            });
                        }

                    });
                }

                List<TaskModel> udpDatagram = task.getUdpDatagram();
                if (ListUtil.GtZero(udpDatagram)) {
                    udpDatagram.forEach(taskModel -> {

                    });
                }

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