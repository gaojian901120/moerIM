package com.moer.socket;

import java.util.concurrent.*;

public class AsyncThreadPool {
    private static final ExecutorService executor = new ThreadPoolExecutor(8, 8, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(100000));//CPU核数4-10倍
    public static void submit(Runnable runnable){
        Future f = executor.submit(runnable);

    }
}
