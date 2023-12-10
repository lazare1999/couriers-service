package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.repository.OrderJobsRepository;
import com.lazo.couriers.app.orders.repository.OrdersRepository;
import com.lazo.couriers.app.user.repository.UserRepository;
import com.lazo.couriers.utils.LazoUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.List;

import static com.lazo.couriers.utils.LazoUtils.getCurrentApplicationUserId;

/**
 * Created by Lazo on 2021-10-13
 */

@Service
@RequiredArgsConstructor
public class OrdersBuyerServiceImpl implements OrdersBuyerService {

    HttpHeaders headers = new HttpHeaders();

    private final OrdersRepository ordersRepository;
    private final OrderJobsRepository orderJobsRepository;

    private final UserRepository userRepository;

    private String getUserName() {
        var userId = getCurrentApplicationUserId();
        if (userId ==null)
            return "";

        var userName = userRepository.findUsernameByUserId(Long.valueOf(userId));

        if (StringUtils.isEmpty(userName))
            return "";

        return userName;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<List<OrdersDomain>> getBuyerParcels(Integer pageKey, Integer pageSize) {

        if (pageKey ==null || pageSize ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var userName = getUserName();
        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var page = ordersRepository.findAll((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.ACTIVE));
            predicate = builder.and(predicate, builder.equal(root.get("viewerPhone"), userName));
            predicate = builder.and(predicate, root.get("jobId").isNotNull());
            return predicate;
        }, PageRequest.of(pageKey, pageSize, LazoUtils.getSortDesc("orderId")));

        return new ResponseEntity<>(page.toList(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Long> getDoneBuyerParcels() {

        var userName = getUserName();
        if (StringUtils.isEmpty(userName))
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var ans = ordersRepository.count((root, query, builder) -> {
            Predicate predicate = builder.conjunction();
            predicate = builder.and(predicate, builder.equal(root.get("orderStatus"), OrdersDomain.OrderStatus.DONE));
            predicate = builder.and(predicate, builder.equal(root.get("viewerPhone"), userName));
            return predicate;
        });

        return new ResponseEntity<>(ans, headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Long> getCourierUserIdByJob(Long jobId) {
        if (jobId == null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        var jobCourierUserId = orderJobsRepository.getCourierUserId(jobId);

        if (jobCourierUserId ==null)
            return new ResponseEntity<>(null, headers, HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(jobCourierUserId, headers, HttpStatus.OK);
    }

}
