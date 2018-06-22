package com.baojie.sched.service.task.basetask;

import com.baojie.sched.service.Business;
import com.baojie.sched.service.task.taskmonitor.Monitor4Task;
import com.baojie.sched.threadpool.FutureCancel;
import com.baojie.sched.threadpool.PoolShutDown;
import com.baojie.sched.threadpool.ScheduledPool;
import com.baojie.sched.threadpool.TFactory;
import com.baojie.sched.threadpool.ThreadPool;
import com.baojie.sched.util.Sleep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public abstract class BaseTask extends TaskConfig {
    private static final Logger log = LoggerFactory.getLogger(BaseTask.class);
    private static final Map<String, Semaphore> sems = new ConcurrentHashMap<>(64);
    protected final ThreadLocalRandom r = ThreadLocalRandom.current();
    protected final CopyOnWriteArrayList<ScheduledFuture<?>> sfs = new CopyOnWriteArrayList<>();
    protected final AtomicBoolean stop = new AtomicBoolean(false);
    protected final ReentrantLock mainLock = new ReentrantLock();
    protected final ScheduledPool sp;
    protected final ThreadPool tp;
    protected final Business bus;
    protected final String name;
    protected final Monitor m;
    protected volatile int delay = 3600;

    protected BaseTask(String name, Business bus) {
        final Semaphore sem = new Semaphore(1);
        init(sem);
        putSem(name, sem);
        this.name = name;
        this.bus = bus;
        this.sp = scheduledPool(name);
        this.tp = threadPool(name);
        this.m = new Monitor(name, sem);
        this.m.startMonitor();
        doScheduled();
    }

    private void putSem(String name, Semaphore sem) {
        if (null == name || null == sem) {
            throw new NullPointerException();
        } else {
            sems.putIfAbsent(name, sem);
        }
    }

    private ScheduledPool scheduledPool(String name) {
        ScheduledPool sp = new ScheduledPool(2, TFactory.create(name + "_scheduled_task"));
        sp.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        sp.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        sp.setRemoveOnCancelPolicy(true);
        return sp;
    }

    private ThreadPool threadPool(String name) {
        int gtns = threadNum(name);
        return new ThreadPool(gtns, 128, 60, TimeUnit.SECONDS, new SynchronousQueue<>(),
                TFactory.create(name + "_query_task"));
    }

    private void init(Semaphore sem) {
        try {
            sem.acquire(1);
        } catch (InterruptedException e) {
            log.error(e.toString(), e);
            throw new Error(e.getCause());
        } catch (Throwable t) {
            log.error(t.toString(), t);
            throw new Error(t.getCause());
        }
    }

    public int initialDelay(int delay) {
        int r0 = r.nextInt(100, 180);
        int r1 = r.nextInt(1, 33);
        if (r0 == 0) {
            r0 = 6;
        }
        return (delay / r0) + r1;
    }

    public void doScheduled() {
        throw new IllegalStateException();
    }

    protected void superDestory() {
        final ReentrantLock lock = mainLock;
        lock.lock();
        try {
            if (stop.get()) {
                return;
            }
            stop.set(true);
            release(name);
            FutureCancel.cancelScheduled(sfs, true);
            PoolShutDown.scheduledPool(sp, name + "_sched_task");
            PoolShutDown.threadPool(tp, name + "_query_task");
            sems.remove(name);
        } finally {
            lock.unlock();
        }
    }

    public static void release(String name) {
        if (null == name || name.length() == 0) {
            return;
        } else {
            final Semaphore s = sems.get(name);
            if (null == s) {
                return;
            } else {
                s.release(1);
            }
        }
    }

    private final class Monitor extends Monitor4Task {
        private final Semaphore sem;

        protected Monitor(String mn, Semaphore sem) {
            super(mn + "_monitor");
            this.sem = sem;
        }

        @Override
        public void monitor() {
            for (; ; ) {
                acquire();
                if (stop.get()) {
                    return;
                }
                int d = delayDura(name);
                if (d == delay) {
                    if (stop.get()) {
                        return;
                    } else {
                        checkFuture();
                    }
                } else {
                    stopService();
                    if (stop.get()) {
                        return;
                    } else {
                        rebuild();
                    }
                }
            }
        }

        private void acquire() {
            if (stop.get()) {
                return;
            }
            try {
                sem.tryAcquire(1, delay, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error(e.toString(), e);
            } catch (Throwable t) {
                log.error(t.toString(), t);
            }
        }

        private void checkFuture() {
            final ReentrantLock lock = mainLock;
            lock.lock();
            try {
                if (stop.get()) {
                    return;
                }
                CopyOnWriteArrayList<ScheduledFuture<?>> list = sfs;
                for (ScheduledFuture<?> sf : list) {
                    if (null == sf) {
                        continue;
                    } else {
                        if (!stop.get() && sf.isDone()) {
                            list.remove(sf);
                            FutureCancel.cancelScheduled(sf);
                            sp.purge();
                            doScheduled();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        private void stopService() {
            for (int i = 0; i < 300; i++) {
                if (stop.get()) {
                    return;
                }
                FutureCancel.cancelScheduled(sfs, true);
                sp.purge();
                if (0 == sp.getActiveCount()) {
                    return;
                } else {
                    Sleep.locksptSleep(TimeUnit.MILLISECONDS, 10);
                }
            }
        }

        private void rebuild() {
            final ReentrantLock lock = mainLock;
            lock.lock();
            try {
                log.debug("********  scheduled task monitor=" + getName() + ", start rebuild  ********");
                if (stop.get()) {
                    return;
                }
                int live = sp.getActiveCount();
                if (0 != live) {
                    log.error("asyn active alive=" + live + ", occur Error, also rebuild");
                }
                int ls = sfs.size();
                if (ls == 0) {
                    doScheduled();
                } else {
                    log.error("list size of future=" + ls + ", do nothing");
                }
                log.debug("********  scheduled task monitor=" + getName() + ", finish rebuild  ********");
            } finally {
                lock.unlock();
            }
        }

    }

}
