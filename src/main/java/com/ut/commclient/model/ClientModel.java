package com.ut.commclient.model;

import java.io.PrintWriter;
import java.net.Socket;

public class ClientModel {
    private String ip;
    private int port;
    private Socket socket;
    private PrintWriter writer;
    private long lastRecTime;

    public ClientModel() {
    }

    public ClientModel(String ip, int port, Socket socket, PrintWriter writer, long lastRecTime) {
        this.ip = ip;
        this.port = port;
        this.socket = socket;
        this.writer = writer;
        this.lastRecTime = lastRecTime;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    public long getLastRecTime() {
        return lastRecTime;
    }

    public void setLastRecTime(long lastRecTime) {
        this.lastRecTime = lastRecTime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public String toString() {
        return ip;
    }
}
