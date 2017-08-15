/**
 *   @function:$
 *   @description: $
 *   @param:$
 *   @return:$
 *   @history:
 * 1.date:$ $
 *           author:$
 *           modification:
 */

package com.cw.library.compressor.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池的管理类
 *
 * @author Cw
 * @date 17/8/15
 */
public class ThreadPoolManager {

    public static ThreadProxyPool threadProxyPool;

    public static ThreadProxyPool getThreadProxyPool() {
        return getThreadProxyPool(3, 5, 5L);
    }

    public static ThreadProxyPool getThreadProxyPool(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        if (threadProxyPool == null) {
            synchronized (ThreadPoolManager.class) {
                if (threadProxyPool == null) {
                    threadProxyPool = new ThreadProxyPool(corePoolSize, maximumPoolSize, keepAliveTime);
                }
            }
        }
        return threadProxyPool;
    }

    /**
     * 用于执行线程中的任务，以及管理线程
     */
    public static class ThreadProxyPool {
        private int corePoolSize;
        private int maximumPoolSize;
        private long keepAliveTime;
        private ThreadPoolExecutor threadPoolExecutor;

        public ThreadProxyPool(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
            this.corePoolSize = corePoolSize;
            this.maximumPoolSize = maximumPoolSize;
            this.keepAliveTime = keepAliveTime;
        }

        /**
         * 将线程中所要执行的代码封装到任务中，然后给此方法执行
         */
        public void excute(Runnable runnable) {
            if (runnable != null) {
                if (threadPoolExecutor == null || threadPoolExecutor.isShutdown()) {
                    threadPoolExecutor = new ThreadPoolExecutor(corePoolSize,// 核心线程数
                            maximumPoolSize,// 最大线程数
                            keepAliveTime, // 非核心线程存活时间
                            TimeUnit.MICROSECONDS, // 线程存活时间单位
                            new LinkedBlockingQueue<Runnable>(), // 任务队列
                            Executors.defaultThreadFactory());
                }
                // 执行任务
                threadPoolExecutor.execute(runnable);
            }
        }

        /***
         * 从线程池中将任务移除
         *
         * @param downloadTask 被移除的任务
         */
        public void cancelRunnable(Runnable downloadTask) {
            if (downloadTask != null) {
                // 线程池是否还在运行
                if (threadPoolExecutor != null && !threadPoolExecutor.isShutdown()) {
                    // 获取线程池中等待的队列
                    BlockingQueue<Runnable> queue = threadPoolExecutor.getQueue();
                    queue.remove(downloadTask);
                }
            }
        }

        /**
         * 启动一次顺序关闭，执行以前提交的任务，但不接受新任务。
         */
        public void shutdown() {
            if (threadPoolExecutor != null) {
                threadPoolExecutor.shutdown();
                threadPoolExecutor = null;
            }
        }

        /**
         * 如果shutdown后所有任务都已完成，则返回 true。
         */
        public boolean isTerminated() {
            if (threadPoolExecutor != null) {
                return threadPoolExecutor.isTerminated();
            }
            return true;
        }
    }
}