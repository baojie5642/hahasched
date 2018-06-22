package com.baojie.sched.util;

import com.jsoniter.JsonIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class MapUtil {
    private static final Logger log = LoggerFactory.getLogger(MapUtil.class);

    private MapUtil() {
        throw new IllegalArgumentException();
    }

    public static String getValue(Map<String, String> map, String key) {
        if (null == map) {
            return "";
        }
        if (null == key) {
            return "";
        }
        if (key.length() <= 0) {
            return "";
        }
        try {
            return map.get(key);
        } catch (Throwable te) {
            log.error(te.toString() + ", key=" + key, te);
        }
        return "";
    }

    public static Map<String, String> j2m(String json, String idmobile) {
        try {
            return JsonIterator.deserialize(json, Map.class);
        } catch (Throwable te) {
            log.error(te.toString() + ", oid or mobile=" + idmobile + ", json=" + json, te);
        }
        return null;
    }


}
