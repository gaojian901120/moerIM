package com.moer.l2;

import com.moer.handler.MessageDispatchHandler;

import java.util.concurrent.*;

/**
 * Created by gaoxuejian on 2018/5/3.
 */
public class DispatchServer {
    /**
     * 优先级队列 透传消息>大V消息>普通群组消息>普通私信>小秘书群发私信
     */
    private static final ExecutorService executor = new PriorityThreadPoolExecutor(16, 40, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(100000));//CPU核数4-10倍

    public static void dispatchMsg(MessageDispatchHandler task) {
        executor.submit(task);
    }

}

class PriorityThreadPoolExecutor extends ThreadPoolExecutor {
    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                      RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                      ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public PriorityThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                      long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                      ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new ComparableFutureTask<>(runnable, value);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        return new ComparableFutureTask<>(callable);
    }

    protected class ComparableFutureTask<V>
            extends FutureTask<V> implements Comparable<ComparableFutureTask<V>> {
        private Object object;

        public ComparableFutureTask(Callable<V> callable) {
            super(callable);
            object = callable;
        }

        public ComparableFutureTask(Runnable runnable, V result) {
            super(runnable, result);
            object = runnable;
        }

        @Override
        @SuppressWarnings("unchecked")
        public int compareTo(ComparableFutureTask<V> o) {
            if (this == o) {
                return 0;
            }
            if (o == null) {
                return -1; // high priority
            }
            if (object != null && o.object != null) {
                if (object.getClass().equals(o.object.getClass())) {
                    if (object instanceof Comparable) {
                        return ((Comparable) object).compareTo(o.object);
                    }
                }
            }
            return 0;
        }
    }
}



