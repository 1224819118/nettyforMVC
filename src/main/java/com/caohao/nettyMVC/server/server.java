package com.caohao.nettyMVC.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;

public class server {
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ChannelFuture future = null;
    public void start(int port){
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            future = serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new serverIntaizered())
                    .handler(new LoggingHandler())
                    .bind(port).sync();
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.err.println("server start error, one exception hapaned");
            System.err.println("exception message:"+e.getMessage());
        }
    }
    public void stopServer(){
        try {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            System.err.println("server stop error, one exception hapaned");
            System.err.println("exception message:"+e.getMessage());
        }
    }
}
