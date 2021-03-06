package com.ut.commclient.component;

import com.ut.commclient.common.BufferedWriterLock;
import com.ut.commclient.config.Config;
import com.ut.commclient.model.RecModel;
import com.ut.commclient.constant.HeartBeat;
import com.ut.commclient.util.FileUtil;
import com.ut.commclient.util.ResUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

@EqualsAndHashCode(callSuper = true)
@Data
@Log4j
public class TcpClientTab extends Tab {
    private Button beginBtn;
    private Button stopBtn;
    private Button sendBtn;
    private TextField ipTxt;
    private TextField portTxt;
    private TextArea sendMsgTxt;
    private TextArea recMsgTxt;
    private BufferedWriterLock writer;
    private Socket socket;
    private BufferedReader reader;
    private Long lastEchoTime;
    private Boolean isStop;

    public TcpClientTab() {
        ipTxt = new TextField("127.0.0.1");
        ipTxt.setPrefWidth(100);

        portTxt = new TextField("8998");
        portTxt.setPrefWidth(50);

        beginBtn = new Button("开始连接");

        stopBtn = new Button("关闭连接");
        stopBtn.setDisable(true);

        sendMsgTxt = new TextArea();
        sendMsgTxt.setPrefHeight(100);
//        sendMsgTxt.setPrefWidth(300);

        sendBtn = new Button("发送信息");
//        sendBtn.setMinWidth(70);
        sendBtn.setDisable(true);

        recMsgTxt = new TextArea();
        recMsgTxt.setEditable(false);

        //第1行
        HBox hBox = new HBox();
        hBox.setSpacing(10);
//        hBox.setPadding(new Insets(5));
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(ipTxt, portTxt, beginBtn, stopBtn, sendBtn);

        //第2行
        HBox hBox2 = new HBox();
        hBox2.setSpacing(10);
//        hBox2.setPadding(new Insets(5));
        hBox2.setAlignment(Pos.CENTER);
        hBox2.getChildren().addAll(sendMsgTxt);

        //总体布局
        VBox vBox = new VBox();
        vBox.setMaxWidth(500);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(5));
        vBox.getChildren().addAll(hBox, hBox2, recMsgTxt);

        this.setContent(vBox);

        beginBtn.setOnAction(actionEvent -> connectBegin());

        stopBtn.setOnAction(actionEvent -> connectEnd());

        sendBtn.setOnAction(actionEvent -> sendMsg());

        this.setOnCloseRequest(event -> {
            isStop = true;
            ResUtil.closeWriterAndReaderAndSocket(reader, writer, socket);
        });
    }

    public void connectBegin() {
        //获取ip与端口
        String ip = ipTxt.getText();
        int port = Integer.parseInt(portTxt.getText());

        //禁止按钮
        beginBtn.setDisable(true);
        stopBtn.setDisable(false);
        isStop = false;

        //开启线程,连接服务端
        new Thread(() -> connectToServer(ip, port)).start();

    }

    private void connectToServer(String ip, int port) {
        //连接服务器，失败则会一直尝试重连
        for (int i = 1; ; i++) {
            try {
                socket = new Socket(ip, port);
                writer = new BufferedWriterLock(new OutputStreamWriter(socket.getOutputStream()));
                break;
            } catch (Exception e) {
                e.printStackTrace();
                if (isStop || i == 5) return;
                //停止多长时间后再重试
                try {
                    Thread.sleep(HeartBeat.TIME_RETRY);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                recMsgTxt.appendText("连接失败：" + e.getMessage() + "重试次数：" + i + "\n");
                try {
                    log.error(InetAddress.getLocalHost().getHostAddress() + "===>" + ip + ":" + port, e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        //连接成功
        stopBtn.setDisable(false);
        sendBtn.setDisable(false);
        recMsgTxt.appendText("连接成功" + "\n");

        //开启接收信息线程
        new Thread(() -> {
            try {
                //构造输入流
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                char[] chars = new char[1024];

                //循环读取，此处会阻塞
                int len;
                while ((len = reader.read(chars)) != -1) {
                    //读出来并打印
                    String msg = new String(chars, 0, len);

                    //如果收到心跳包则进行相应的处理并忽略此信息
                    if (msg.equals(HeartBeat.ECHO_SERVER) || msg.equals(HeartBeat.ECHO_CLIENT)) {
                        System.out.println(msg);
                        heartBeatHandler(msg);
                        continue;
                    }

                    recMsgTxt.appendText(msg + "\n");
                    System.out.println(msg);

                    //写入文件
                    RecModel recModel = new RecModel("Server",
                            socket.getInetAddress().getHostAddress(),
                            socket.getPort(),
                            "client",
                            socket.getLocalAddress().getHostAddress(),
                            socket.getLocalPort(),
                            msg);
                    FileUtil.write(Config.recPath, recModel);
//                    FileUtil.write(".\\rec\\rec.txt", recModel);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e);
                ResUtil.closeReader(reader);
            }
        }).start();

        //开启心跳线程
        new Thread(() -> heartBeat(ip, port)).start();
    }

    private void heartBeatHandler(String heartBeatStr) {
        //如果是心跳包的回应则刷新时间，否则是心跳包则回复本机IP过去
        if (heartBeatStr.equals(HeartBeat.ECHO_SERVER)) {
            lastEchoTime = System.currentTimeMillis();
        } else {
            writer.writeFlush(HeartBeat.ECHO_CLIENT + ":" + socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort());
        }
    }

    private void heartBeat(String ip, int port) {
        //先初始化第一次时间
        lastEchoTime = System.currentTimeMillis();
        while (!socket.isClosed()) {
            try {
                Thread.sleep(HeartBeat.TIME_PAUSE);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //当前时间大于最后一次接收到心跳包的时间就停止连接并且重新尝试连接
            if (System.currentTimeMillis() - lastEchoTime > HeartBeat.TIME_OUT) {
                recMsgTxt.appendText("对方断线" + "\n");
                sendBtn.setDisable(true);
                ResUtil.closeWriterAndReaderAndSocket(reader, writer, socket);
                //新开启线程，重新连接服务端
                new Thread(() -> connectToServer(ip, port)).start();
                break;
            }
            //发送心跳包
            writer.writeFlush(HeartBeat.ECHO_SERVER);
        }
    }

    public void connectEnd() {
        isStop = true;
        beginBtn.setDisable(false);
        stopBtn.setDisable(true);
        sendBtn.setDisable(true);
        recMsgTxt.appendText("停止连接" + "\n");

        ResUtil.closeWriterAndReaderAndSocket(reader, writer, socket);
    }

    public void sendMsg() {
        String msg = sendMsgTxt.getText();
        writer.writeFlush(msg);
    }
}
