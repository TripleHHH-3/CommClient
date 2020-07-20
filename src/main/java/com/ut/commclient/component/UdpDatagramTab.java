package com.ut.commclient.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpDatagramTab extends Tab {
    TextField ipTxt;
    TextField sendPortTxt;
    TextArea recMsgTxt;
    Button bindBtn;
    Button stopBindBtn;
    Button sendBtn;
    DatagramSocket sendSocket;
    DatagramSocket recSocket;
    TextArea sendMsgTxt;
    TextField recPortTxt;
    Button listenBtn;
    Button stopListenBtn;

    public UdpDatagramTab() {
        ipTxt = new TextField("127.0.0.1");
        ipTxt.setPrefWidth(100);

        sendPortTxt = new TextField("9999");
        sendPortTxt.setPrefWidth(50);

        bindBtn = new Button("绑定目标");

        stopBindBtn = new Button("停止绑定");
        stopBindBtn.setDisable(true);

        sendBtn = new Button("发送信息");
        sendBtn.setDisable(true);


        sendMsgTxt = new TextArea();
        sendMsgTxt.setPrefHeight(100);

        recPortTxt = new TextField("9999");
        recPortTxt.setPrefWidth(50);

        listenBtn = new Button("开始监听");

        stopListenBtn = new Button("停止监听");
        stopListenBtn.setDisable(true);

        recMsgTxt = new TextArea();
        recMsgTxt.setEditable(false);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(ipTxt, sendPortTxt, bindBtn, stopBindBtn, sendBtn);

        HBox hBox2 = new HBox();
        hBox2.setSpacing(10);
        hBox2.setAlignment(Pos.CENTER);
        hBox2.getChildren().addAll(recPortTxt, listenBtn, stopListenBtn);

        VBox vBox = new VBox();
        vBox.setMaxWidth(500);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(5));
        vBox.getChildren().addAll(hBox, sendMsgTxt, hBox2, recMsgTxt);

        this.setContent(vBox);

        bindBtn.setOnAction(actionEvent -> bindBegin());

        sendBtn.setOnAction(actionEvent -> sendMsg());

        stopBindBtn.setOnAction(actionEvent -> bindEnd());

        listenBtn.setOnAction(actionEvent -> listenBegin());

        stopListenBtn.setOnAction(actionEvent -> listenEnd());

        this.setOnCloseRequest(event -> {
            if (sendSocket != null) sendSocket.close();
        });
    }

    private void bindBegin() {
        bindBtn.setDisable(true);
        try {
            sendSocket = new DatagramSocket();
            stopBindBtn.setDisable(false);
            sendBtn.setDisable(false);
            sendPortTxt.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
            if (sendSocket != null) sendSocket.close();
            bindBtn.setDisable(false);
        }
    }

    private void bindEnd() {
        sendSocket.close();
        bindBtn.setDisable(false);
        stopBindBtn.setDisable(true);
        sendBtn.setDisable(true);
        sendPortTxt.setDisable(false);
    }

    private void sendMsg() {
        String ip = ipTxt.getText();
        byte[] bytes = sendMsgTxt.getText().getBytes();
        int port = Integer.parseInt(sendPortTxt.getText());
        //2.创建数据包对象

        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(ip);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length,inetAddress,port);

        //3.发送数据包
        try {
            sendSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenBegin() {
        int port = Integer.parseInt(recPortTxt.getText());

        listenBtn.setDisable(true);
        //开启绑定线程
        new Thread(() -> {

            //1.创建DatagramSocket数据报包套接字对象
            try {
                recSocket = new DatagramSocket(port);

                stopListenBtn.setDisable(false);

                while (true) {
                //创建一个数据包用于接收服务端返回的信息
                    byte[] bytes = new byte[1024];
                    DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);

                    //接收消息，会阻塞
                    recSocket.receive(datagramPacket);
                    //解析消息并打印数据
                    String recStr = new String(datagramPacket.getData());
                    System.out.println(recStr);
                    System.out.println(datagramPacket.getAddress());
                    System.out.println(datagramPacket.getPort());
                    recMsgTxt.appendText(recStr + "\n");

                }

            } catch (Exception e) {
                e.printStackTrace();
                if(recSocket!=null)recSocket.close();
                listenBtn.setDisable(false);
            }

        }).start();
    }

    private void listenEnd() {
        listenBtn.setDisable(false);
        stopListenBtn.setDisable(true);
        recSocket.close();
    }
}