package com.baojie.sched.executor;

import com.baojie.sched.jpa.entity.HahaOrder;
import com.baojie.sched.jpa.repos.HahaOrderRepos;
import com.baojie.sched.jpa.spec.SpecType;
import com.baojie.sched.jpa.spec.SpecialQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class HahaOrderExecutor {
    private static final Logger log = LoggerFactory.getLogger(HahaOrderExecutor.class);
    private final HahaOrderRepos haha;

    private HahaOrderExecutor(HahaOrderRepos haha) {
        if (null == haha) {
            throw new NullPointerException();
        } else {
            this.haha = haha;
        }
    }

    public static HahaOrderExecutor create(HahaOrderRepos haha) {
        return new HahaOrderExecutor(haha);
    }

    public HahaOrder findById(long id) {
        try {
            return haha.findById(id);
        } catch (Throwable te) {
            log.error(te.toString() + ", id=" + id, te);
        }
        return null;
    }

    public Page<HahaOrder> delayOrder(int pageNum) {
        Pageable page = specPage(pageNum);
        SpecialQuery sp = new SpecialQuery(SpecType.DELAY.value());
        try {
            return haha.findAll(sp, page);
        } catch (Throwable e) {
            log.error(e.toString(), e);
        }
        return null;
    }

    private Pageable specPage(int pageNum) {
        final Sort sort = new Sort(Sort.Direction.ASC, "id");
        final Pageable page = PageRequest.of(pageNum, 500, sort);
        return page;
    }

    public HahaOrder save(HahaOrder order) {
        return update(order);
    }

    public HahaOrder update(HahaOrder order) {
        if (null == order) {
            return null;
        }
        try {
            return haha.save(order);
        } catch (Throwable e) {
            log.error(e.toString() + ", to DB order=" + order, e);
        }
        return null;
    }

}
