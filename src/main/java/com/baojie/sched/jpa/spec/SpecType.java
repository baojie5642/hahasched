package com.baojie.sched.jpa.spec;

public enum SpecType {

    DELAY(0);

    private final int value;

    SpecType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public String toString() {
        return "SpecType{" +
                "value=" + value +
                '}';
    }

}
