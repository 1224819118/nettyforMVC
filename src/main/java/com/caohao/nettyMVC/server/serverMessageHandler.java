package com.caohao.nettyMVC.server;

import com.caohao.nettyMVC.util.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class serverMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private int UPTIME_COUNT = 0;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)){
                if (UPTIME_COUNT>=3){
                    System.err.println("three time uptime connection close");
                    ctx.channel().close();
                }else
                    UPTIME_COUNT+=1;
            }
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("one channel inbound");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        if (msg instanceof FullHttpRequest){
            String uri = msg.uri();
            HttpMethod method = msg.method();
            System.out.println("request uri:"+uri+" and request method:"+method.name());
            if (method.equals(HttpMethod.POST)){
                ByteBuf content = msg.content();
                int len = content.readableBytes();
                byte[] message = new byte[len];
                content.readBytes(message);
                System.out.println(new String(message));
                String o = (String) byUriToInvokeMethod(uri, new String(message));
                System.out.println("result is "+o);
                ByteBuf byteBuf = byUriGetFileBytebuf("/" + o + ".html");
                FullHttpResponse send = byBytebufToReponse(byteBuf);
                ctx.channel().writeAndFlush(send);
                return;
            }else if (method.equals(HttpMethod.GET)){
                if (uri.indexOf(".")>0){
                    ByteBuf byteBuf = byUriGetFileBytebuf(uri);
                    ctx.writeAndFlush(byBytebufToReponse(byteBuf));
                    return;
                }else {
                    Object o = byUriToInvokeMethod(uri, null);
                    ctx.channel().writeAndFlush((String)o);
                    return;
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("one execption hapand,shutdown this connection");
        System.err.println("exception message:"+cause.getMessage());
        cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("one channel unregister");
    }

    public ByteBuf byUriGetFileBytebuf(String uri){
        ByteBuf buffer = Unpooled.buffer();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream("/Users/caohao/test/httpserver"+uri);
            int code=0;
            while ((code = inputStream.read())!=-1){
                buffer.writeByte(code);
            }
        } catch (IOException e) {
            System.err.println("one ecxeption hanend in nativefile reading and exception message is "+e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                System.err.println("one ecxeption hanend in nativefile closeing and exception message is "+e.getMessage());
            }
        }
        return buffer;
    }
    public FullHttpResponse byBytebufToReponse(ByteBuf byteBuf){
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,byteBuf);
    }
    public Object byUriToInvokeMethod(String uri,String target){
        String[] uris = uri.split("/");//这里我们规定uri的第一个部分是类名。第二个部分是方法名，这个方法用于将一系列的业务逻辑整合最好得出最终数据
        System.out.println(uris.length);
        for (String s:uris){
            System.out.println(s);
        }
        Class targetClass = null;
        String methodResult = null;
        try {
            targetClass = Class.forName("com.caohao.nettyMVC.service."+uris[1]);
        } catch (ClassNotFoundException e) {
            System.err.println("cont find this class"+e.getMessage());
        }
        try {
            if (target!=null&&!target.equals("")){
                Method method = targetClass.getMethod(uris[2], Message.class);
                Message message = Message.getMessage(target);
                methodResult = (String) method.invoke(targetClass.newInstance(),message);
            }else {
                Method method = targetClass.getMethod(uris[2]);
                methodResult = (String) method.invoke(targetClass.newInstance());
            }
            return methodResult;
        } catch (NoSuchMethodException e) {
            System.err.println("cont find this method message is "+e.getMessage());
        } catch (IllegalAccessException e) {
            System.err.println(""+e.getMessage());
        } catch (InvocationTargetException e) {
            System.err.println(""+e.getMessage());
        } catch (InstantiationException e) {
            System.err.println(""+e.getMessage());
        }
        return null;
    }
}
