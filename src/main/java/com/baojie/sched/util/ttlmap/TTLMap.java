package com.baojie.sched.util.ttlmap;

import com.baojie.sched.threadpool.FutureCancel;
import com.baojie.sched.threadpool.PoolShutDown;
import com.baojie.sched.threadpool.TFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class TTLMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = 7249069246733562397L;

    private static final int SIZE = 4096;
    private static final int WORK_NUM = 2;
    private static final String BASE_NAME = "_ttlmap";

    private static final Logger log = LoggerFactory.getLogger(TTLMap.class);

    private final ArrayList<Future<?>> futures = new ArrayList<>(4);
    private final AtomicBoolean close = new AtomicBoolean(false);
    private final DelayQueue<TTLKey<K>> delayQueue = new DelayQueue<>();
    private final AtomicInteger size = new AtomicInteger(0);
    private final ThreadPoolExecutor pool;
    private final String name;

    public TTLMap(String name) {
        super();
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(WORK_NUM);
        this.pool.setThreadFactory(TFactory.create(name + BASE_NAME));
        expireRunner();
    }

    private void expireRunner() {
        for (int i = 0; i < WORK_NUM; i++) {
            GetDelayRunner r = new GetDelayRunner(delayQueue);
            futures.add(pool.submit(r));
        }
    }

    public boolean put(K key, V value, long ttl, TimeUnit unit) {
        if (!pass(key, value, ttl, unit)) {
            return false;
        }
        if (isClose()) {
            log.warn("map close, key=" + key + ", value=" + value);
            return false;
        }
        int s = size();
        if (s >= SIZE) {
            log.warn("map full, size=" + s + ", key=" + key + ", value=" + value);
            return false;
        }
        return putCache(key, value, ttl, unit);
    }

    private boolean pass(K key, V value, long ttl, TimeUnit unit) {
        if (null == key || null == value) {
            return false;
        }
        if (0 >= ttl || null == unit) {
            return false;
        }
        return true;
    }

    private boolean putCache(K key, V value, long ttl, TimeUnit timeUnit) {
        TTLKey<K> ttlKey = new TTLKey<>(key, ttl, timeUnit);
        // 采用唯一性原则，相同的key只保存一个副本缓存
        if (null == super.putIfAbsent(key, value)) {
            delayQueue.put(ttlKey);
            size.incrementAndGet();
            return true;
        } else {
            return false;
        }
    }

    public V get(Object key) {
        if (null == key) {
            return null;
        }
        if (close.get()) {
            return null;
        } else {
            return super.get(key);
            // 如果缓存被拿走，但是还没有到达失效时间，不会删除缓存
            // decreSize(v);
            // return v;
        }
    }

    private void removeCache(K k) {
        if (null != k) {
            V v = super.remove(k);
            decreSize(v);
        }
    }

    private void decreSize(V v) {
        if (null != v) {
            size.decrementAndGet();
        }
    }

    public int size() {
        return size.get();
    }

    public boolean isClose() {
        return close.get();
    }

    public String name() {
        return name;
    }

    public void closeCache() {
        if (close.get()) {
            return;
        } else {
            if (close.compareAndSet(false, true)) {
                closeAll();
            }
        }
    }

    private void closeAll() {
        try {
            closeFuture();
            clean();
        } finally {
            shuntDownPool();
        }
    }

    private void closeFuture() {
        FutureCancel.cancel(futures, true);
    }

    private void clean() {
        super.clear();
        delayQueue.clear();
    }

    private void shuntDownPool() {
        PoolShutDown.executor(pool, name + BASE_NAME);
    }

    private final class GetDelayRunner implements Runnable {

        private final DelayQueue<TTLKey<K>> dq;

        public GetDelayRunner(DelayQueue<TTLKey<K>> dq) {
            this.dq = dq;
        }

        @Override
        public void run() {
            try {
                work();
            } finally {
                if (isClose()) {
                    clean();
                }
            }
        }

        private void work() {
            TTLKey<K> ttlKey = null;
            for (; ; ) {
                if (isClose()) {
                    break;
                } else {
                    ttlKey = getDelay();
                    if (null == ttlKey) {
                        continue;
                    } else {
                        removeCache(ttlKey.getKey());
                    }
                }
            }
        }

        private TTLKey<K> getDelay() {
            try {
                return dq.take();
            } catch (Exception e) {
                log.error(e.toString(), e);
            } catch (Throwable t) {
                log.error(t.toString(), t);
            }
            return null;
        }

    }

}
