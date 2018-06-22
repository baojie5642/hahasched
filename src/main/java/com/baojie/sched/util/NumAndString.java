package com.baojie.sched.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NumAndString {
    private static final Logger log = LoggerFactory.getLogger(NumAndString.class);

    private NumAndString() {

    }

    public static String l2s(final Long l) {
        if (null == l) {
            log.error("long is null");
            return "";
        }
        try {
            return String.valueOf(l);
        } catch (Throwable t) {
            log.error(t.toString() + ", long=" + l, t);
        }
        return "";
    }

    public static long s2l(final String s) {
        if (null == s) {
            log.error("string null");
            return -1L;
        }
        try {
            return Long.parseLong(s);
        } catch (Throwable t) {
            log.error(t.toString() + ", String=" + s, t);
            return -1L;
        }
    }

    public static String int2s(final Integer i) {
        if (null == i) {
            log.error("integer null");
            return "";
        }
        try {
            return String.valueOf(i);
        } catch (Throwable t) {
            log.error(t.toString() + ", int=" + i, t);
        }
        return "";
    }

    public static int s2int(final String s) {
        if (null == s) {
            log.error("string null");
            return -1;
        }
        try {
            return Integer.parseInt(s);
        } catch (Throwable t) {
            log.error(t.toString() + ", String=" + s, t);
            return -1;
        }
    }

}
