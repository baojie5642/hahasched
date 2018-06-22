package com.baojie.sched.worker.basework;

import com.baojie.sched.service.Business;
import com.baojie.sched.service.task.basetask.TaskConfig;
import com.baojie.sched.threadpool.FutureCancel;
import com.baojie.sched.threadpool.ThreadPool;
import com.baojie.sched.util.Dict;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseSchedWork<T> extends TaskConfig {
    protected static final Logger log = LoggerFactory.getLogger(BaseSchedWork.class);
    protected final String name;
    protected final Business bus;
    protected final ThreadPool tp;
    protected final AtomicBoolean stop;

    protected BaseSchedWork(String name, Business bus, ThreadPool tp, AtomicBoolean stop) {
        if (null == bus) {
            throw new IllegalArgumentException();
        }
        if (null == name || name.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.tp = tp;
        this.bus = bus;
        this.stop = stop;
        this.name = name;
    }

    protected void scheduled() {
        if (stop.get() || buttonOff()) {
            return;
        }
        if (apiOff()) {
            return;
        }
        Queue<T> ols = query();
        if (null == ols) {
            log.error(name + ", query queue null");
            return;
        }
        int size = ols.size();
        log.debug(name + ", query queue size=" + size);
        if (0 >= size) {
            return;
        } else {
            startPush(ols);
        }
    }

    protected boolean buttonOff() {
        String b = taskButton(name);
        if (!StringUtils.isBlank(b) && Dict.API_OFF.equals(b)) {
            log.debug(name + ", found button has off, value=" + b);
            return true;
        } else {
            return false;
        }
    }

    protected boolean apiOff() {
        String value = bus.apiValue();
        if (null == value) {
            return true;
        } else {
            if (!StringUtils.isBlank(value) && Dict.API_OFF.equals(value)) {
                log.debug(name + " found api has off, value=" + value);
                return true;
            } else {
                return false;
            }
        }
    }

    public Queue<T> query() {
        List<T> content = null;
        Page<T> page = null;
        final ConcurrentLinkedQueue<T> q = new ConcurrentLinkedQueue<>();
        for (int i = 0; ; i++) {
            if (stop.get()) {
                // 如果检测到停止信号
                // 跳出循环
                break;
            }
            page = queryDB(i);
            if (null == page) {
                break;
            }
            if (!page.hasContent()) {
                break;
            } else {
                content = page.getContent();
            }
            if (null == content) {
                continue;
            }
            int cs = content.size();
            log.debug("name=" + name + ", page num=" + i + ", size=" + cs);
            if (0 >= cs) {
                continue;
            } else {
                list2Queue(content, q);
            }
            if (page.isLast() || !page.hasNext()) {
                break;
            }
        }
        // 如果检测到停止信号
        // 那么直接clean队列中的数据
        if (stop.get()) {
            q.clear();
        }
        return q;
    }

    // 不能随便clean spring data jpa 查询结果中的content
    private void list2Queue(List<T> content, Queue<T> queue) {
        for (T t : content) {
            if (null == t) {
                continue;
            } else {
                queue.offer(t);
            }
        }
    }

    public Page<T> queryDB(int pageNum) {
        throw new IllegalArgumentException();
    }

    public Runnable create(Queue<T> q, CountDownLatch l, AtomicBoolean f) {
        throw new IllegalArgumentException();
    }

    private void startPush(Queue<T> queue) {
        int tn = threadNum(name);
        final CountDownLatch latch = new CountDownLatch(tn);
        final List<Future<?>> futureList = new ArrayList<>(tn);
        final AtomicBoolean flag = new AtomicBoolean(false);
        Runnable r = null;
        try {
            for (int i = 0; i < tn; i++) {
                r = create(queue, latch, flag);
                if (!tp.isShutdown()) {
                    submit(r, latch, futureList);
                } else {
                    latch.countDown();
                }
            }
        } finally {
            waitLatch(latch, futureList, flag);
        }
    }

    private void submit(Runnable r, CountDownLatch l, List<Future<?>> fs) {
        Future<?> f = null;
        try {
            f = tp.submit(r);
            if (!f.isDone()) {
                fs.add(f);
            } else {
                l.countDown();
            }
        } catch (Throwable t) {
            l.countDown();
            log.error(t.toString(), t);
        }
    }

    private void waitLatch(CountDownLatch l, List<Future<?>> fls, AtomicBoolean f) {
        if (stop.get()) {
            cancel(l, fls, f);
        } else {
            await(l, fls, f);
        }
    }

    private void cancel(CountDownLatch l, List<Future<?>> fls, AtomicBoolean f) {
        try {
            f.set(true);
            int s = fls.size();
            for (int i = 0; i < s; i++) {
                l.countDown();
            }
        } finally {
            FutureCancel.cancel(fls, true);
        }
    }

    private void await(CountDownLatch l, List<Future<?>> fls, AtomicBoolean f) {
        boolean suc = false;
        try {
            suc = l.await(6, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            log.error(e.toString(), e);
        } catch (Throwable t) {
            log.error(t.toString(), t);
        } finally {
            if (!suc) {
                f.set(true);
                FutureCancel.cancel(fls, true);
            }
        }
    }

}
