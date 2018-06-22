package com.baojie.sched.service.task;

import com.baojie.sched.service.Business;
import com.baojie.sched.service.task.basetask.BaseTask;
import com.baojie.sched.service.task.basetask.TaskName;
import com.baojie.sched.worker.scheduled.DelaySched;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class DelayTask extends BaseTask {

    @Autowired
    public DelayTask(Business bus) {
        super(TaskName.Delay.name(), bus);
    }

    @Override
    public void doScheduled() {
        final DelaySched sr = DelaySched.create(bus, tp, stop);
        delay = delayDura(name);
        ScheduledFuture<?> sf = sp.scheduleWithFixedDelay(sr, initialDelay(delay), delay, TimeUnit.SECONDS);
        if (!sf.isDone()) {
            sfs.add(sf);
        }
    }

    @PreDestroy
    private void destory() {
        superDestory();
    }
}