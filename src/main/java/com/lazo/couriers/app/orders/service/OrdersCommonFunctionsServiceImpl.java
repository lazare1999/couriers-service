package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.repository.OrderJobsRepository;
import com.lazo.couriers.app.orders.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by Lazo on 2021-10-08
 */

@Service
@RequiredArgsConstructor
public class OrdersCommonFunctionsServiceImpl implements OrdersCommonFunctionsService{

    private final OrdersRepository ordersRepository;
    private final OrderJobsRepository orderJobsRepository;

    @Override
    public OrdersDomain getOrder(Long orderId) {
        if (orderId==null)
            return null;

        var order0 = ordersRepository.findById(orderId);
        if (order0.isEmpty())
            return null;
        return order0.get();
    }

    @Override
    public OrderJobsDomain validateJob(Long jobId) {
        if (jobId ==null)
            return null;

        var order0 = orderJobsRepository.findById(jobId);
        if (order0.isEmpty())
            return null;

        return order0.get();
    }

}
