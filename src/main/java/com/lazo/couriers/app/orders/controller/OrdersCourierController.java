package com.lazo.couriers.app.orders.controller;

import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.models.JobsModel;
import com.lazo.couriers.app.orders.service.OrdersCourierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by Lazo on 2021-06-29
 */

@RestController
@RequestMapping("orders_courier")
@RequiredArgsConstructor
public class OrdersCourierController {

    private final OrdersCourierService ordersCourierService;

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/get_courier_jobs" })
    public ResponseEntity<JobsModel> getCourierJobs(@RequestHeader("Authorization") String token) {
        return ordersCourierService.getCourierJobs(token);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/get_active_courier_jobs" })
    public ResponseEntity<List<OrderJobsDomain>> getActiveCourierJobs(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize) {
        return ordersCourierService.getActiveCourierJobs(token, pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/get_handed_over_jobs_courier" })
    public ResponseEntity<List<OrderJobsDomain>> getHandedOverJobsCourier(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize) {
        return ordersCourierService.getHandedOverJobsCourier(token, pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/get_done_courier_jobs" })
    public ResponseEntity<List<OrderJobsDomain>> getDoneCourierJobs(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize) {
        return ordersCourierService.getDoneCourierJobs(token, pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/courier_accept_handed_over_job" })
    public ResponseEntity<String> courierAcceptHandedOverJob(Long orderJobId) {
        return ordersCourierService.handedOverJobDispenserForCourier(orderJobId, true);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/courier_not_accept_handed_over_job" })
    public ResponseEntity<String> courierNotAcceptHandedOverJob(Long orderJobId) {
        return ordersCourierService.handedOverJobDispenserForCourier(orderJobId, false);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/get_courier_parcels" })
    public ResponseEntity<List<OrdersDomain>> getCourierParcels(@RequestHeader("Authorization") String token) {
        return ordersCourierService.getParcels(token, true);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/get_not_courier_parcels" })
    public ResponseEntity<List<OrdersDomain>> getNotCourierParcels(@RequestHeader("Authorization") String token) {
        return ordersCourierService.getParcels(token, false);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/get_parcels_by_job" })
    public ResponseEntity<List<OrdersDomain>> getParcelsByJob(Integer pageKey, Integer pageSize, Long orderJobId) {
        return ordersCourierService.getParcelsByJob(pageKey, pageSize, orderJobId);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/approve_job" })
    public ResponseEntity<String> approveJob(Long orderJobId) {
        return ordersCourierService.approveJob(orderJobId);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/get_vacant_jobs" })
    public ResponseEntity<List<OrderJobsDomain>> getVacantJobs() {
        return ordersCourierService.getVacantJobs();
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/set_parcel_as_unsuccessful" })
    public ResponseEntity<Boolean> setParcelAsUnsuccessful(Long orderId) {
        return ordersCourierService.setParcelAsUnsuccessful(orderId);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/take_parcel" })
    public ResponseEntity<Boolean> takeParcel(Long orderId) {
        return ordersCourierService.takeParcel(orderId);
    }

    @PreAuthorize("hasRole('ROLE_COURIER')")
    @RequestMapping({ "/jobs_done" })
    public ResponseEntity<Boolean> jobsDone(Long orderId) {
        return ordersCourierService.jobsDone(orderId);
    }

}
