package com.baojie.sched.worker.scheduled;

import com.baojie.sched.jpa.entity.HahaOrder;
import com.baojie.sched.service.Business;
import com.baojie.sched.service.task.basetask.TaskName;
import com.baojie.sched.threadpool.ThreadPool;
import com.baojie.sched.worker.basework.BaseSchedWork;
import org.springframework.data.domain.Page;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class DelaySched extends BaseSchedWork<HahaOrder> implements Runnable {

    private DelaySched(Business bus, ThreadPool tp, AtomicBoolean stop) {
        super(TaskName.Delay.name(), bus, tp, stop);
    }

    public static DelaySched create(Business bus, ThreadPool tp, AtomicBoolean stop) {
        return new DelaySched(bus, tp, stop);
    }

    @Override
    public void run() {
        scheduled();
    }

    @Override
    public Page<HahaOrder> queryDB(int pageNum) {
        return bus.delayOrder(pageNum);
    }

    @Override
    public Runnable create(Queue<HahaOrder> q, CountDownLatch l, AtomicBoolean f) {
        return DelaySubmit.create(q, f, l, bus);
    }

}
