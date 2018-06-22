package com.baojie.sched.service;

import com.baojie.sched.service.hotload.Config;
import com.baojie.sched.jpa.entity.HahaOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;

@Service
public class Business {
    private static final Logger log = LoggerFactory.getLogger(Business.class);

    private final Config conf;
    private final HahaOrderService haha;

    @Autowired
    public Business(Config conf, HahaOrderService haha) {
        this.conf = conf;
        this.haha = haha;
    }

    @PreDestroy
    private void destory() {

    }

    public void business(HahaOrder order) {

    }

    public String apiValue() {
        return "";
    }

    public Page<HahaOrder> delayOrder(int pageNum) {
        return haha.delayOrder(pageNum);
    }

}
