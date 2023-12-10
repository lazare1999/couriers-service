package com.lazo.couriers.app.orders.controller;

import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.service.OrdersBuyerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Lazo on 2021-10-13
 */

@RestController
@RequestMapping("orders_buyer")
@RequiredArgsConstructor
public class OrdersBuyerController {

    private final OrdersBuyerService ordersBuyerService;

    @PreAuthorize("hasRole('ROLE_BUYER')")
    @RequestMapping({ "/buyer_parcels" })
    public ResponseEntity<List<OrdersDomain>> getBuyerParcels(Integer pageKey, Integer pageSize) {
        return ordersBuyerService.getBuyerParcels(pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_BUYER')")
    @RequestMapping({ "/done_buyer_parcels" })
    public ResponseEntity<Long> getDoneBuyerParcels() {
        return ordersBuyerService.getDoneBuyerParcels();
    }

    @PreAuthorize("hasRole('ROLE_BUYER')")
    @RequestMapping({ "/get_courier_user_id_by_job_id" })
    public ResponseEntity<Long> getCourierUserIdByJobId(Long jobId) {
        return ordersBuyerService.getCourierUserIdByJob(jobId);
    }

}
