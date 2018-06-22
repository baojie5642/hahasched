package com.baojie.sched.service.task.basetask;

import com.baojie.sched.service.hotload.Config;
import com.baojie.sched.util.Dict;

public abstract class TaskConfig {

    protected TaskConfig() {

    }

    protected int threadNum(String name) {
        if (TaskName.Delay.name().equals(name)) {
            return Config.getDelayTn();
        } else {
            return 4;
        }
    }

    protected int delayDura(String name) {
        if (TaskName.Delay.name().equals(name)) {
            return Config.getDelayDura();
        } else {
            return 180;
        }
    }

    protected String taskButton(String name) {
        if (TaskName.Delay.name().equals(name)) {
            return Config.getDelayButton();
        } else {
            return Dict.API_OFF;
        }
    }

}
