package com.ut.commclient.component;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.*;

public class UdpDatagramTab extends Tab {
    //    MulticastSocket socket = null;
    TextField ipTxt;
    TextField portTxt;
    TextArea recMsgTxt;
    Button bindBtn;
    Button stopBtn;
    Button sendBtn;
    DatagramSocket socket;
    TextArea sendMsgTxt;

    public UdpDatagramTab() {
        ipTxt = new TextField("127.0.0.1");
        ipTxt.setPrefWidth(100);

        portTxt = new TextField("9999");
        portTxt.setPrefWidth(50);

        bindBtn = new Button("绑定目标");

        stopBtn = new Button("停止绑定");
        stopBtn.setDisable(true);

        sendBtn = new Button("发送信息");
        sendBtn.setDisable(true);


        sendMsgTxt = new TextArea();
        sendMsgTxt.setPrefHeight(100);

        recMsgTxt = new TextArea();
        recMsgTxt.setEditable(false);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(ipTxt, portTxt, bindBtn, stopBtn, sendBtn);

        VBox vBox = new VBox();
        vBox.setMaxWidth(500);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(5));
        vBox.getChildren().addAll(hBox, sendMsgTxt, recMsgTxt);

        this.setContent(vBox);

        bindBtn.setOnAction(actionEvent -> bindBegin());

        sendBtn.setOnAction(actionEvent -> sendMsg());

        stopBtn.setOnAction(actionEvent -> bingEnd());

        this.setOnCloseRequest(event -> {
            if (socket != null) socket.close();
        });
    }

    private void bindBegin() {
        int port = Integer.parseInt(portTxt.getText());

        bindBtn.setDisable(true);
        //开启绑定线程
        new Thread(() -> {

            //1.创建DatagramSocket数据报包套接字对象
            try {
                socket = new DatagramSocket(port);

                stopBtn.setDisable(false);
                sendBtn.setDisable(false);
                //创建一个数据包用于接收服务端返回的信息
                byte[] bytes = new byte[1024];
                DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);

                while (true) {
                    try {
                        //接收消息，会阻塞
                        socket.receive(datagramPacket);
                        //解析消息并打印数据
                        String recStr = new String(datagramPacket.getData());
                        System.out.println(recStr);
                        System.out.println(datagramPacket.getAddress());
                        System.out.println(datagramPacket.getPort());
                        recMsgTxt.appendText(recStr + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }

                }
                //Closes this datagram socket
                socket.close();
            } catch (SocketException e) {
                e.printStackTrace();
            } finally {
                bindBtn.setDisable(false);
            }

        }).start();
    }


    private void sendMsg() {
        String ip = ipTxt.getText();
        byte[] bytes = sendMsgTxt.getText().getBytes();
        int port = Integer.parseInt(portTxt.getText());
        //2.创建数据包对象

        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(ip);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
        packet.setAddress(inetAddress);
        packet.setPort(port);

        //3.发送数据包
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bingEnd() {
        socket.close();
        bindBtn.setDisable(false);
        stopBtn.setDisable(true);
        sendBtn.setDisable(true);
    }
}