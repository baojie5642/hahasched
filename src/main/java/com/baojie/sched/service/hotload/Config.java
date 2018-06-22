package com.baojie.sched.service.hotload;

import com.baojie.sched.util.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class Config {
    private static final Logger log = LoggerFactory.getLogger(Config.class);

    /*******************************   delay   *****************************/
    // 延迟订单的定时查询数据库的时间间隔
    @HotReloadable("${haha.delay.dura}")
    private static volatile int delayDura = 180;
    // 延迟订单，处理在数据库中查询出来的订单的线程数
    @HotReloadable("${haha.delay.tn}")
    private static volatile int delayTn = 4;
    // 延迟订单的开关
    @HotReloadable("${haha.delay.button}")
    private static volatile String delayButton = "off";

    private final ConfigMonitor monitor = new ConfigMonitor("sched_config_monitor");
    private final ReentrantLock mainLock = new ReentrantLock();
    private final AtomicBoolean stop = new AtomicBoolean(false);
    private final Semaphore signal = new Semaphore(1);
    private final PropertiesManager pm;
    private final ResourceHandler rh;

    @Autowired
    public Config(ResourceHandler reh) {
        if (null == reh) {
            throw new NullPointerException();
        }
        init();
        this.rh = reh;
        this.pm = new PropertiesManager(rh);
        this.pm.startHotLoad(signal);
        flushMillis(1);
        this.monitor.keep();
        signal.release(1);
        log.info("sched_config_manager is working");
    }

    private void init() {
        try {
            signal.acquire(1);
        } catch (InterruptedException e) {
            log.error(e.toString(), e);
            throw new IllegalStateException();
        } catch (Throwable t) {
            log.error(t.toString(), t);
            throw new IllegalStateException();
        }
    }

    private void flushMillis(long millis) {
        Thread.yield();
        Sleep.locksptSleep(TimeUnit.MILLISECONDS, millis);
        Thread.yield();
    }

    @PreDestroy
    public void destory() {
        if (stop.get()) {
            return;
        } else {
            if (stop.compareAndSet(false, true)) {
                shutDown();
                log.info("sched_config_monitor stop completed");
            } else {
                return;
            }
        }
    }

    private void shutDown() {
        try {
            pm.destory();
        } finally {
            signal.release(1);
        }
    }

    private void print() {
        flushMillis(30);
        final ReentrantLock lock = mainLock;
        lock.lock();
        try {
            log.debug("/*****************************  start print config info  *******************************/");
            log.debug("delay threadNum=" + getDelayTn());
            log.debug("delay duration=" + getDelayDura());
            log.debug("delay button=" + getDelayButton());
            log.debug("/*****************************  finish print config info  *******************************/");
        } finally {
            lock.unlock();
        }
    }

    // 没有使用三目表达式
    /*********************************************************/

    public static int getDelayDura() {
        int delay = delayDura;
        if (0 >= delay) {
            return 180;
        } else {
            return delay;
        }
    }

    public static int getDelayTn() {
        int delay = delayTn;
        if (0 >= delay) {
            return 4;
        } else {
            return delay;
        }
    }

    public static String getDelayButton() {
        String b = delayButton;
        if (null == b) {
            return "off";
        } else {
            return b.trim();
        }
    }

    /*********************************************************/

    private final class ConfigMonitor extends KeepVolatile4Config {

        public ConfigMonitor(String name) {
            super(name);
        }

        @Override
        public void work() {
            try {
                for (; ; ) {
                    acquire();
                    if (stop.get()) {
                        return;
                    } else {
                        print();
                    }
                }
            } finally {
                log.debug(name + " has stopped");
            }
        }

        private void acquire() {
            if (stop.get()) {
                return;
            }
            try {
                signal.acquire(1);
            } catch (InterruptedException e) {
                error(e);
            } catch (Throwable t) {
                error(t);
            }
        }

        private void error(Throwable t) {
            if (!stop.get()) {
                log.error(t.toString(), t);
                // 如果没有显示的中断，那么擦除中断状态
                Thread.currentThread().interrupted();
            }
        }

    }

    private abstract class KeepVolatile4Config implements Runnable {

        protected final Logger log = LoggerFactory.getLogger(KeepVolatile4Config.class);
        private final CountDownLatch latch = new CountDownLatch(1);
        protected final Thread thread;
        protected final String name;

        protected KeepVolatile4Config(String name) {
            if (null == name || name.length() == 0) {
                throw new IllegalStateException();
            }
            this.name = name;
            this.thread = new Thread(this, name);
        }

        public void keep() {
            try {
                thread.start();
            } finally {
                latch.countDown();
            }
        }

        public void work() {
            throw new IllegalArgumentException();
        }

        @Override
        public void run() {
            try {
                waitLatch();
            } finally {
                work();
            }
        }

        private void waitLatch() {
            boolean suc = false;
            try {
                suc = latch.await(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                log.error(e.toString(), e);
            } catch (Throwable t) {
                log.error(t.toString(), t);
            }
            if (suc) {
                return;
            } else {
                throw new Error("prop monitor latch");
            }
        }
    }

}
