package com.caohao.nettyMVC.server;

public class main {
    public static void main(String[] args) {
        server server = new server();
        server.start(9001);
        server.stopServer();
    }
}
