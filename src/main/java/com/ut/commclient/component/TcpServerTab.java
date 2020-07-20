package com.ut.commclient.component;

import com.ut.commclient.constant.HeartBeat;
import com.ut.commclient.model.ClientModel;
import com.ut.commclient.util.ResUtil;
import com.ut.commclient.util.TcpServerThread;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

@EqualsAndHashCode(callSuper = true)
@Data
public class TcpServerTab extends Tab {
    private Button beginBtn;
    private Button stopBtn;
    private TextField portTxt;
    private TextArea recTxt;
    private ServerSocket serverSocket = null;
    private Button sendBtn;
    private TextArea sendMsgTxt;
    private ListView<ClientModel> clientListView;

    public TcpServerTab() {
        Label portLb = new Label("端口:");
        portTxt = new TextField("8998");
        portTxt.setPrefWidth(50);

        beginBtn = new Button("开始监听");

        stopBtn = new Button("停止监听");
        stopBtn.setDisable(true);

        sendBtn = new Button("发送信息");
        sendBtn.setDisable(true);

        sendMsgTxt = new TextArea();
        sendMsgTxt.setPrefHeight(100);

        clientListView = new ListView<>();
        clientListView.setPrefHeight(150);

        recTxt = new TextArea();
        recTxt.setEditable(false);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
//        hBox.setPadding(new Insets(5));
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(portLb, portTxt, beginBtn, stopBtn, sendBtn);

        HBox hBox2 = new HBox();
        hBox2.setSpacing(10);
        hBox2.setAlignment(Pos.CENTER);
        hBox2.getChildren().addAll(sendMsgTxt, clientListView);

        VBox vBox = new VBox();
        vBox.setMaxWidth(500);
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(5));
        vBox.getChildren().addAll(hBox, hBox2, recTxt);

        this.setContent(vBox);

        beginBtn.setOnAction(actionEvent -> listenBegin());

        stopBtn.setOnAction(actionEvent -> listenEnd());

        sendBtn.setOnAction(actionEvent -> sendMsg());

        this.setOnCloseRequest(event -> listenEnd());
    }

    public void listenBegin() {
        int port = Integer.parseInt(portTxt.getText());

        beginBtn.setDisable(true);
        sendBtn.setDisable(false);

        //开启监听线程
        new Thread(() -> {
            try {
                //服务端在xxx端口监听客户端的TCP连接请求
                serverSocket = new ServerSocket(port);

                //监听成功后，开启心跳线程
                new Thread(this::heartBeat).start();
                stopBtn.setDisable(false);

                while (true) {
                    //等待客户端的连接，会阻塞
                    Socket client = serverSocket.accept();

                    //打印成功连接
                    recTxt.appendText("与客户端连接成功！" + client.getInetAddress().getHostAddress() + "\n");

                    //把连接进来的客户端放进队列
                    ClientModel clientModel = new ClientModel(
                            client.getInetAddress().getHostAddress(),
                            client.getPort(),
                            client,
                            new PrintWriter(client.getOutputStream(), true),
                            System.currentTimeMillis()
                    );

                    Platform.runLater(() -> clientListView.getItems().add(clientModel));

                    //为每个客户端的连接开启一个线程
                    new Thread(new TcpServerThread(client, recTxt, clientListView)).start();
                }

            } catch (Exception e) {
                if (e instanceof BindException) recTxt.appendText("端口已占用" + "\n");

                beginBtn.setDisable(false);
                stopBtn.setDisable(true);
                recTxt.appendText("停止监听" + "\n");

                ResUtil.closeServerSocket(serverSocket);
                e.printStackTrace();
            }

        }).start();
    }

    private void heartBeat() {
        while (!serverSocket.isClosed()) {

            ObservableList<ClientModel> clientList = clientListView.getItems();
            if (clientList != null && clientList.size() > 0) {
                for (ClientModel client : clientList) {
                    if (System.currentTimeMillis() - client.getLastRecTime() > HeartBeat.TIME_OUT) {
                        ResUtil.closeWriterAndSocket(client.getWriter(), client.getSocket());
                        Platform.runLater(() -> clientList.remove(client));
                    }
                }
            }

            //必须把暂停放在此处，防止并发异常
            try {
                Thread.sleep(HeartBeat.TIME_PAUSE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (clientList != null && clientList.size() > 0) {
                clientList.forEach(client -> client.getWriter().println(HeartBeat.ECHO_CLIENT));
            }
        }
    }

    private void sendMsg() {
        ClientModel client = clientListView.getSelectionModel().getSelectedItem();
        if (client != null) {
            client.getWriter().println(sendMsgTxt.getText());
        }
    }

    public void listenEnd() {
        //关闭服务端
        ResUtil.closeServerSocket(serverSocket);

        //关闭客户端
        ObservableList<ClientModel> clientList = clientListView.getItems();
        if (clientList != null && clientList.size() > 0) {
            clientList.forEach(client -> {
                ResUtil.closeWriterAndSocket(client.getWriter(), client.getSocket());
                Platform.runLater(() -> clientList.remove(client));
            });
        }

        beginBtn.setDisable(false);
        stopBtn.setDisable(true);
        sendBtn.setDisable(true);
    }
}
