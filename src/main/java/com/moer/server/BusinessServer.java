package com.moer.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by gaoxuejian on 2018/5/1.
 * 业务线程池
 */
public class BusinessServer {
    private static final ExecutorService executor = new ThreadPoolExecutor(20, 100, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100000));//CPU核数4-10倍

    public static void doBusiness(ChannelHandlerContext ctx, Object msg) {
        //异步线程池处理
        executor.submit(() -> {
            try {
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    System.out.println("Mmethod: " + request.method());
                    System.out.println("User agent: " + request.headers().get("User-Agent"));
                    System.out.println("Content length: " + request.headers().get("content-length"));
                    Thread.currentThread().setName("buessness-thread");
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK, Unpooled.wrappedBuffer("I am ok"
                            .getBytes()));
                    response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
                    response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
                    response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    ctx.write(response);
                    ctx.flush();
                }
            } catch (Exception e) {


            }
        });
    }
}
