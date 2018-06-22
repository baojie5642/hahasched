package com.baojie.sched.util.ttlmap;

import java.io.Serializable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class TTLKey<K> implements Delayed, Serializable {
    private static final long serialVersionUID = -2017031516252955555L;
    private static final AtomicLong SEQUENCER = new AtomicLong();
    private final TimeUnit delayUnit;
    private final Long sequenceNum;
    private final Long delayTime;
    private final Long takeTime;
    private final K key;

    protected TTLKey(K key, long delayTime, TimeUnit delayUnit) {
        innerCheck(key, delayTime, delayUnit);
        this.takeTime = takeTime(delayTime, delayUnit);
        this.sequenceNum = SEQUENCER.getAndIncrement();
        this.delayUnit = delayUnit;
        this.delayTime = delayTime;
        this.key = key;
    }

    private void innerCheck(K key, long delayTime, TimeUnit delayUnit) {
        if (null == key) {
            throw new NullPointerException("'key' must not be null");
        }
        if (delayTime <= 0L) {
            throw new IllegalStateException("'delayTime' <= 0, key=" + key);
        }
        if (null == delayUnit) {
            throw new NullPointerException("'delayTimeUnit' must not be null");
        }
    }

    private long takeTime(long delayTime, TimeUnit delayTimeUnit) {
        return TimeUnit.NANOSECONDS.convert(delayTime, delayTimeUnit) + System.nanoTime();
    }

    @Override
    public long getDelay(final TimeUnit unit) {
        return unit.convert(takeTime - now(), TimeUnit.NANOSECONDS);
    }

    private final long now() {
        return System.nanoTime();
    }

    // 这里的写法参见scheduledpool
    @Override
    public int compareTo(final Delayed other) {
        if (null == other) {
            return 1;
        }
        if (other == this) {
            return 0;
        }
        if (other instanceof TTLKey) {
            TTLKey x = (TTLKey) other;
            long diff = takeTime - x.getTakeTime();
            if (diff < 0) {
                return -1;
            } else if (diff > 0) {
                return 1;
            } else if (sequenceNum < x.getSequenceNum()) {
                return -1;
            } else {
                return 1;
            }
        }
        final long diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
        return (diff < 0) ? -1 : ((diff > 0) ? 1 : 0);
    }

    public long getSequenceNum() {
        return sequenceNum;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public long getTakeTime() {
        return takeTime;
    }

    public TimeUnit getDelayUnit() {
        return delayUnit;
    }

    public K getKey() {
        return key;
    }

    public boolean isTTL() {
        final long remain = takeTime - System.nanoTime();
        if (remain <= 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "TTLKey{" +
                "delayUnit=" + delayUnit +
                ", sequenceNum=" + sequenceNum +
                ", delayTime=" + delayTime +
                ", takeTime=" + takeTime +
                ", key=" + key +
                '}';
    }

}
