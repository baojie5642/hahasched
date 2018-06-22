package com.baojie.sched.service.task.basetask;

public enum TaskName {

    Delay(0);

    private final int code;

    TaskName(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return String.valueOf(code);
    }

}
