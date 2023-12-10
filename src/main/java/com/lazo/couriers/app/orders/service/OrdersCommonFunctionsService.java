package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;

/**
 * Created by Lazo on 2021-10-08
 */


public interface OrdersCommonFunctionsService {

    OrdersDomain getOrder(Long orderId);

    OrderJobsDomain validateJob(Long jobId);

}
