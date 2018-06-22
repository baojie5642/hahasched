package com.baojie.sched.jpa.spec;

import com.baojie.sched.jpa.entity.HahaOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

public class SpecialQuery implements Specification<HahaOrder> {

    private static final Logger log = LoggerFactory.getLogger(SpecialQuery.class);

    private final int value;

    public SpecialQuery(int value) {
        this.value = value;
    }

    @Override
    public Predicate toPredicate(Root<HahaOrder> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
        return switchType(root, cb);
    }

    private Predicate switchType(Root<HahaOrder> root, CriteriaBuilder cb) {
        /**
         * Path<String> rstep = root.get(R_STEP);
         * Path<Integer> rstate = root.get(R_STATE);
         * Path<Date> rct = root.get(R_CREATE_TIME);
         * Predicate step_p = cb.equal(rstep, "step");
         * Predicate state_p = cb.greaterThanOrEqualTo(rstate, 1);
         * long now = System.currentTimeMillis();
         * Date start = startTime(now);
         * Date end = endTime(now);
         * Predicate rct_p_0 = cb.greaterThanOrEqualTo(rct, start);
         * Predicate rct_p_1 = cb.lessThan(rct, end);
         * return cb.and(step_p, state_p, rct_p_0, rct_p_1);
         **/
        return null;
    }

}
