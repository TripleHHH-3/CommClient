package com.ut.commclient.util;

import com.ut.commclient.constant.HeartBeat;
import com.ut.commclient.model.ClientModel;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.BufferedReader;
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
                String[] split = msgStr.replaceAll("(\r\n|\r|\n|\n\r)", "").split(":");
                if (split[0].equals("echo")) {
                    System.out.println(msgStr);
                    heartBeatHandler(split);
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

    private void heartBeatHandler(String[] split) {
        if (split[1].equals("server")) {
            clientListView.getItems().forEach(client -> {
                if (client.getSocket().equals(this.client)) {
                    client.getWriter().println(HeartBeat.ECHO_SERVER);
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
