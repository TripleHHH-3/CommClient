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
    Button bindBeginBtn;
    Button bindStopBtn;
    TextField bindIpGroupTxt;
    TextField bindPortTxt;
    TextArea recTxt;
    MulticastSocket recSocket;
    MulticastSocket sendSocket;
    TextArea sendMsgTxt;
    Button sendBtn;
    Button listenBeginBtn;
    Button listenStopBtn;
    TextField listenIpGroupTxt;
    TextField listenPortTxt;

    public UdpMulticastTab() {
        bindIpGroupTxt = new TextField("224.255.10.0");
        bindIpGroupTxt.setPrefWidth(100);

        bindPortTxt = new TextField("9999");
        bindPortTxt.setPrefWidth(50);

        bindBeginBtn = new Button("绑定群组");

        bindStopBtn = new Button("停止绑定");
        bindStopBtn.setDisable(true);

        sendBtn = new Button("发送信息");
        sendBtn.setDisable(true);

        sendMsgTxt = new TextArea();
        sendMsgTxt.setPrefHeight(100);

        listenIpGroupTxt = new TextField("224.255.10.0");
        listenIpGroupTxt.setPrefWidth(100);

        listenPortTxt = new TextField("9999");
        listenPortTxt.setPrefWidth(50);

        listenBeginBtn = new Button("监听群组");

        listenStopBtn = new Button("停止监听");
        listenStopBtn.setDisable(true);

        recTxt = new TextArea();
        recTxt.setEditable(false);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
//        hBox.setPadding(new Insets(5));
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(bindIpGroupTxt, bindPortTxt, bindBeginBtn, bindStopBtn, sendBtn);

        HBox hBox2 = new HBox();
        hBox2.setSpacing(10);
        hBox2.setAlignment(Pos.CENTER);
        hBox2.getChildren().addAll(listenIpGroupTxt, listenPortTxt, listenBeginBtn, listenStopBtn);

        VBox vBox = new VBox();
        vBox.setMaxWidth(500);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(5));
        vBox.getChildren().addAll(hBox, sendMsgTxt, hBox2, recTxt);

        this.setContent(vBox);

        bindBeginBtn.setOnAction(actionEvent -> bindBegin());

        sendBtn.setOnAction(actionEvent -> sendMsg());

        bindStopBtn.setOnAction(actionEvent -> bindEnd());

        listenBeginBtn.setOnAction(actionEvent -> listenBegin());

        listenStopBtn.setOnAction(actionEvent -> listenEnd());

        this.setOnCloseRequest(event -> {
            if (recSocket != null) recSocket.close();
            if (sendSocket != null) sendSocket.close();
        });
    }

    private void bindBegin() {
        // todo 绑定失败的处理
        try {
            sendSocket = new MulticastSocket();
            bindBeginBtn.setDisable(true);
            bindStopBtn.setDisable(false);
            sendBtn.setDisable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMsg() {
        //todo ip 端口 格式判断
        String ip = bindIpGroupTxt.getText();
        int port = Integer.parseInt(bindPortTxt.getText());

        InetAddress ipGroup;
        try {
            ipGroup = InetAddress.getByName(ip);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        byte[] data = sendMsgTxt.getText().getBytes(); // 声明字节数组
        // 将数据打包
        DatagramPacket packet = new DatagramPacket(data, data.length, ipGroup, port);
        System.out.println(new String(data)); // 将广播信息输出
        try {
            sendSocket.send(packet); // 发送数据
        } catch (Exception e) {
            e.printStackTrace(); // 输出异常信息
        }
    }

    private void bindEnd() {
        if (sendSocket != null) sendSocket.close();
        bindBeginBtn.setDisable(false);
        bindStopBtn.setDisable(true);
        sendBtn.setDisable(true);
    }

    private void listenBegin() {
        //TODO ip 端口 格式判断
        int port = Integer.parseInt(listenPortTxt.getText());
        String ip = listenIpGroupTxt.getText();

        new Thread(() -> {
            try {
                InetAddress ipGroup = InetAddress.getByName(ip); // 指定接收地址
                recSocket = new MulticastSocket(port); // 绑定多点广播套接字
                recSocket.setTimeToLive(1);
                recSocket.joinGroup(ipGroup); // 加入广播组

                listenBeginBtn.setDisable(true);
                listenStopBtn.setDisable(false);

                while (true) {
                    byte[] data = new byte[1024]; // 创建byte数组
                    // 待接收的数据包
                    DatagramPacket packet = new DatagramPacket(data, data.length, ipGroup, port);
                    recSocket.receive(packet); // 接收数据包
                    String message = new String(packet.getData(), 0, packet.getLength()); // 获取数据包中内容
                    // 将接收内容显示在文本域中
                    recTxt.appendText(message + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (recSocket != null) recSocket.close();
            }
        }).start();
    }

    private void listenEnd() {
        listenBeginBtn.setDisable(false);
        listenStopBtn.setDisable(true);
        if (recSocket != null) recSocket.close();
    }
}