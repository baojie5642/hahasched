package com.baojie.sched.service;

import com.baojie.sched.executor.HahaOrderExecutor;
import com.baojie.sched.jpa.entity.HahaOrder;
import com.baojie.sched.jpa.repos.HahaOrderRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class HahaOrderService {
    private final HahaOrderExecutor exe;

    @Autowired
    public HahaOrderService(HahaOrderRepos haha) {
        if (null == haha) {
            throw new IllegalStateException();
        } else {
            this.exe = HahaOrderExecutor.create(haha);
        }
    }

    public HahaOrder findById(long id) {
        return exe.findById(id);
    }

    public Page<HahaOrder> delayOrder(int pageNum) {
        return exe.delayOrder(pageNum);
    }

    public HahaOrder save(HahaOrder order) {
        return update(order);
    }

    public HahaOrder update(HahaOrder order) {
        return exe.update(order);
    }

}
