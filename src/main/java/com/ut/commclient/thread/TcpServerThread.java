package com.ut.commclient.thread;

import com.ut.commclient.constant.HeartBeat;
import com.ut.commclient.model.ClientModel;
import com.ut.commclient.util.ResUtil;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 该类为多线程类，用于服务端
 */
public class TcpServerThread implements Runnable {

    private final Socket client;
    private final TextArea contentTxt;
    private final ListView<ClientModel> clientListView;

    public TcpServerThread(Socket client, TextArea contentTxt, ListView<ClientModel> clientListView) {
        this.client = client;
        this.contentTxt = contentTxt;
        this.clientListView = clientListView;
    }


    public void run() {
        BufferedReader reader = null;
        try {
            //获取Socket的输入流，用来接收从客户端发送过来的数据
            reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));

            char[] chars = new char[1024];
            //接收从客户端发送过来的数据
            int len;
            while ((len = reader.read(chars)) != -1) {
                String msgStr = new String(chars, 0, len);

                //如果是心跳包额外处理，并忽略此信息
                if (msgStr.contains("echo")) {
                    System.out.println(msgStr);
                    heartBeatHandler(msgStr);
                    continue;
                }

                System.out.println(msgStr);
                contentTxt.appendText(msgStr + "\n");
            }
        } catch (Exception e) {
            ResUtil.closeReaderAndSocket(reader, client);
            e.printStackTrace();
        }
    }

    private void heartBeatHandler(String msgStr) {
        String[] split = msgStr.split(":");
        if (split[1].equals("server")) {
            clientListView.getItems().forEach(client -> {
                if (client.getSocket().equals(this.client)) {
                    try {
                        client.getWriter().write(HeartBeat.ECHO_SERVER);
                        client.getWriter().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            clientListView.getItems().forEach(client -> {
                if (client.getIp().equals(split[2]) && client.getPort() == Integer.parseInt(split[3])) {
                    client.setLastRecTime(System.currentTimeMillis());
                }
            });
        }
    }

}
