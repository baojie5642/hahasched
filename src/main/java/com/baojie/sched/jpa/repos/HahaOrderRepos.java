package com.baojie.sched.jpa.repos;

import com.baojie.sched.jpa.entity.HahaOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface HahaOrderRepos extends JpaRepository<HahaOrder, Long>, JpaSpecificationExecutor<HahaOrder> {

    HahaOrder findById(long id);

}
