package com.baojie.sched.service.task.taskmonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class Monitor4Task implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Monitor4Task.class);
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Thread thread;
    private final String name;

    protected Monitor4Task(String name) {
        check(name);
        this.name = name;
        this.thread = new Thread(this, name);
    }

    private void check(String name) {
        if (null == name || name.length() == 0) {
            throw new IllegalArgumentException();
        }
    }

    public abstract void monitor();

    public void startMonitor() {
        try {
            thread.start();
        } finally {
            latch.countDown();
        }
    }

    @Override
    public void run() {
        waitLatch();
        try {
            monitor();
        } finally {
            log.warn("monitor name=" + name + ", has stopped");
        }
    }

    // 60秒内没有启动监控，那么监控自己的线程会抛出异常，但不会影响其他线程
    // 在外层会被子类catch到
    // 子类catch throwable
    private void waitLatch() {
        boolean s = false;
        try {
            s = latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(e.toString(), e);
        } catch (Throwable t) {
            log.error(t.toString(), t);
        }
        if (!s) {
            log.error("wait latch timeout, monitor name=" + name);
            throw new Error();
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Monitor4Task{" +
                "name='" + name + '\'' +
                '}';
    }

}
