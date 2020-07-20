package com.ut.commclient.config;

import com.ut.commclient.common.Host;
import lombok.Data;

import java.util.List;

@Data
public class Config {
    private List<Host> tcpClient;
    private List<Host> tcpServer;
    private List<Host> udpDatagram;
    private List<Host> udpMulticast;

//    @Data
//    public class Broadcast{
//        private List<Host> bind;
//        private List<Host> listen;
//    }
}
