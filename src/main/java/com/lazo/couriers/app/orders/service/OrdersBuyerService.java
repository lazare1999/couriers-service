package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.orders.domain.OrdersDomain;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by Lazo on 2021-10-13
 */

public interface OrdersBuyerService {

    ResponseEntity<List<OrdersDomain>> getBuyerParcels(Integer pageKey, Integer pageSize);

    ResponseEntity<Long> getDoneBuyerParcels();

    ResponseEntity<Long> getCourierUserIdByJob(Long jobId);
}
