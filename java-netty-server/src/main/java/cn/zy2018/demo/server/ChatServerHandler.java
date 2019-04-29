package cn.zy2018.demo.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Created by Administrator on 2019/4/28.
 */
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

        //保存所有活动的用户
        public static final ChannelGroup group = new DefaultChannelGroup(
                GlobalEventExecutor.INSTANCE);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg)
            throws Exception {
        Channel channel = ctx.channel();
        group.forEach(en -> {
            if (en.equals(channel))
            {
                en.writeAndFlush("自己对自己说:" + msg + "\n");
            }else
            {
                en.writeAndFlush(channel.remoteAddress() +"对我说: " + msg + "\n");
            }
        });
    }

    /**客户端连上服务器的时候通知其他的客户端，XXX连上服务器了*/
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        for (Channel ch : group) {
            ch.writeAndFlush(
                    "[" + channel.remoteAddress() + "] " + "is comming");
        }
        group.add(channel);
    }
    /**当客户端断开的时候通知其他的客户端XXX失去服务器端的连接了*/
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        for (Channel ch : group) {
            ch.writeAndFlush(
                    "[" + channel.remoteAddress() + "] " + "is comming");
        }
        group.remove(channel);
    }

    //在建立链接时发送信息
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("[" + channel.remoteAddress() + "] " + "online");
        ctx.writeAndFlush("[server]: welcome");
    }

    //退出链接
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        System.out.println("[" + channel.remoteAddress() + "] " + "offline");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        System.out.println(
                "[" + ctx.channel().remoteAddress() + "]" + "exit the room");
        ctx.close().sync();
    }
}

