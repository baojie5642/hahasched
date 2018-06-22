package com.baojie.sched.worker.scheduled;

import com.baojie.sched.jpa.entity.HahaOrder;
import com.baojie.sched.service.Business;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DelaySubmit implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(DelaySubmit.class);
    private final Queue<HahaOrder> queue;
    private final CountDownLatch latch;
    private final AtomicBoolean stop;
    private final Business bus;

    private DelaySubmit(Queue<HahaOrder> queue, AtomicBoolean stop, CountDownLatch latch, Business bus) {
        this.queue = queue;
        this.latch = latch;
        this.stop = stop;
        this.bus = bus;
    }

    public static DelaySubmit create(Queue<HahaOrder> queue, AtomicBoolean stop, CountDownLatch latch, Business bus) {
        return new DelaySubmit(queue, stop, latch, bus);
    }

    @Override
    public void run() {
        try {
            work();
        } finally {
            release();
        }
    }

    // 如果服务停止直接清空队列
    private void release() {
        try {
            if (stop.get()) {
                queue.clear();
            }
        } finally {
            latch.countDown();
        }
    }

    // 异步查询数据库数据，如果服务停止，可以直接退出
    private void work() {
        try {
            for (; ; ) {
                HahaOrder order = queue.poll();
                if (null == order) {
                    return;
                } else {
                    bus.business(order);
                }
            }
        } finally {
            remain();
        }
    }

    private void remain() {

    }

}