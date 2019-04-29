package cn.zy2018.demo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2019/4/28.
 * 参考：https://blog.csdn.net/chinabestchina/article/details/86633942
 */
public class NettyClientMain {
    private String host;
    private int port;
    private boolean stop = false;

    public NettyClientMain(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args){
        new NettyClientMain("127.0.0.1", 10001).run();
    }

    public void run(){
        //设置一个worker线程，使用
        EventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(worker);
        //指定所使用的 NIO 传输 Channel
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast("stringD", new StringDecoder());
                pipeline.addLast("stringC", new StringEncoder());
                pipeline.addLast("http", new HttpClientCodec());
                pipeline.addLast("chat", new ChatClientHandler());
            }
        });
        try {
            //使用指定的 端口设置套 接字地址
            Channel channel = bootstrap.connect(host, port).sync().channel();
            while (true) {
                //向服务端发送内容
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(System.in));
                String input = reader.readLine();
                if (input != null) {
                    if ("quit".equals(input)) {
                        System.exit(1);
                    }
                    channel.writeAndFlush(input);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}