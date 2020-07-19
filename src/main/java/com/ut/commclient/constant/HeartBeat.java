package com.ut.commclient.constant;

public abstract class HeartBeat {
    /**
     * 用于服务器向客户端发送的心跳包
     */
    public final static String ECHO_CLIENT = "echo:client";

    /**
     * 用于客户端向服务器发送的心跳包
     */
    public final static String ECHO_SERVER = "echo:server";

    /**
     * 心跳包发送间隔时间
     */
    public final static long TIME_PAUSE = 5 * 1000;

    /**
     * 没有收到心跳包的超时时间（默认为3倍心跳包发送间隔时间）
     */
    public final static long TIME_OUT = 2 * TIME_PAUSE;

    /**
     * 连接超时重试间隔时间
     */
    public final static long TIME_RETRY = 1000;
}
