package cn.zy2018.demo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * Created by Administrator on 2019/4/28.
 * 参考：https://blog.csdn.net/chinabestchina/article/details/86633942
 */
public class NettyServerMain {
    private int port;
    public NettyServerMain(int port){
        this.port = port;
    }
    public static void main(String[] args){
        new NettyServerMain(10001).run();
    }
    public void run(){
        //主线程组，接收网络请求
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //worker线程组，对接收到的请求进行读写处理
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //启动服务的启动类（辅助类）
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 添加主线程组和worker线程组_设置循环线程组，前者用于处理客户端连接事件，后者用于处理网络IO
        ServerBootstrap serverBootstrap = bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)  //设置channel为服务端NioServerSocketChannel
                .childHandler(new ChannelInitializer<NioSocketChannel>() { //绑定io事件处理类
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();

                        //以指定分隔符$拆包和粘包
//                        pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("$".getBytes())));
                        //以固定长度拆包和粘包
//                        pipeline.addLast(new FixedLengthFrameDecoder(10));
                        //以换行符拆包和粘包
                        //pipeline.addLast(new LineBasedFrameDecoder(1024));
                        //不指定  不然没有换行符 就服务器的read 就接收不到
                        pipeline.addLast("decode",new StringDecoder());
                        pipeline.addLast("encode",new StringEncoder());
                        pipeline.addLast("chat",new ChatServerHandler());


                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128) //设置日志
                .option(ChannelOption.SO_SNDBUF, 32 * 1024) //设置发送缓存
                .option(ChannelOption.SO_RCVBUF, 32 * 1024)  //接收缓存
                .childOption(ChannelOption.SO_KEEPALIVE, true);//是否保持连接

        try{
            //绑定端口，同步等待成功
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("server strart running in port:" + port);
            //关闭监听端口，同步等待
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 退出
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

