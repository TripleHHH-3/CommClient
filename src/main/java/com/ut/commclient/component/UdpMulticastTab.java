package com.ut.commclient.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class UdpMulticastTab extends Tab {
    Button beginBtn;
    Button stopBtn;
    TextField groupIpTxt;
    TextField portTxt;
    TextArea recTxt;
    MulticastSocket socket;
    TextArea sendMsgTxt;
    Button sendBtn;

    public UdpMulticastTab() {
        groupIpTxt = new TextField("224.255.10.0");
        groupIpTxt.setPrefWidth(100);

        portTxt = new TextField("9999");
        portTxt.setPrefWidth(50);

        beginBtn = new Button("开始监听");

        stopBtn = new Button("停止监听");
        stopBtn.setDisable(true);

        sendBtn = new Button("发送信息");
        sendBtn.setDisable(true);

        sendMsgTxt = new TextArea();
        sendMsgTxt.setPrefHeight(100);

        recTxt = new TextArea();
        recTxt.setEditable(false);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
//        hBox.setPadding(new Insets(5));
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(groupIpTxt, portTxt, beginBtn, stopBtn, sendBtn);

        VBox vBox = new VBox();
        vBox.setMaxWidth(500);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(5));
        vBox.getChildren().addAll(hBox, sendMsgTxt, recTxt);

        this.setContent(vBox);

        beginBtn.setOnAction(actionEvent -> listenBegin());

        sendBtn.setOnAction(actionEvent -> sendMsg());

        stopBtn.setOnAction(actionEvent -> listenEnd());

        this.setOnCloseRequest(event -> {
            if (socket != null) socket.close();
        });
    }

    private void listenBegin() {
        int port = Integer.parseInt(portTxt.getText());
        String groupIp = groupIpTxt.getText();

        beginBtn.setDisable(true);

        new Thread(() -> {
            try {
                InetAddress group;
                group = InetAddress.getByName(groupIp); // 指定接收地址
                socket = new MulticastSocket(port); // 绑定多点广播套接字
                socket.setTimeToLive(1);
                socket.joinGroup(group); // 加入广播组

                stopBtn.setDisable(false);
                sendBtn.setDisable(false);

                while (true) {
                    byte[] data = new byte[1024]; // 创建byte数组
                    // 待接收的数据包
                    DatagramPacket packet = new DatagramPacket(data, data.length, group, port);
                    try {
                        socket.receive(packet); // 接收数据包
                        String message = new String(packet.getData(), 0, packet.getLength()); // 获取数据包中内容
                        // 将接收内容显示在文本域中
                        recTxt.appendText(message + "\n");
                    } catch (Exception e) {
                        e.printStackTrace(); // 输出异常信息
                        recTxt.appendText(e.getMessage() + "\n");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) socket.close();
                beginBtn.setDisable(false);
            }
        }).start();
    }

    private void sendMsg() {
        String groupIp = groupIpTxt.getText();
        int port = Integer.parseInt(portTxt.getText());

        InetAddress group;
        try {
            group = InetAddress.getByName(groupIp);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        byte[] data = sendMsgTxt.getText().getBytes(); // 声明字节数组
        // 将数据打包
        DatagramPacket packet = new DatagramPacket(data, data.length, group, port);
        System.out.println(new String(data)); // 将广播信息输出
        try {
            socket.send(packet); // 发送数据
        } catch (Exception e) {
            e.printStackTrace(); // 输出异常信息
        }
    }

    private void listenEnd() {
        socket.close();
        beginBtn.setDisable(false);
        stopBtn.setDisable(true);
        sendBtn.setDisable(true);
    }
}