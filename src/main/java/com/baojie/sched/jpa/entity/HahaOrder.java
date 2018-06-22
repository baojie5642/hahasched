package com.baojie.sched.jpa.entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;

@Entity
@Table(name = "haha_order")
@Cache(usage = CacheConcurrencyStrategy.NONE)
@Cacheable(false)
public class HahaOrder {

    private static final Logger log = LoggerFactory.getLogger(HahaOrder.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public HahaOrder() {

    }

    public long getId() {
        Long id_ = id;
        if (null == id_) {
            error("id null");
            return -1L;
        } else {
            return id_;
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    private void error(String errInfo) {
        log.error("****=" + errInfo);
    }

    @Override
    public String toString() {
        return "HahaOrder{" +
                "id=" + id +
                '}';
    }

}
