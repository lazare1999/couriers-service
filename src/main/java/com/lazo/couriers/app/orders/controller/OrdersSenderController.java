package com.lazo.couriers.app.orders.controller;

import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.models.*;
import com.lazo.couriers.app.orders.service.OrdersSenderService;
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
@RequestMapping("orders_sender")
@RequiredArgsConstructor
public class OrdersSenderController {

    private final OrdersSenderService ordersSenderService;

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/get_service_price" })
    public ResponseEntity<String> getServicePrice(ServicePriceModel model) {
        return ordersSenderService.getServicePrice(model);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/place_an_order" })
    public ResponseEntity<String> placeAnOrder(OrderModel model) {
        return ordersSenderService.placeAnOrder(model);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/create_job" })
    public ResponseEntity<Object> createJob(@RequestHeader("Authorization") String token, String checkedParcels, Boolean active, Boolean hold) {
        return ordersSenderService.createJob(token, checkedParcels, active, hold);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/get_parcel_and_job_info" })
    public ResponseEntity<ParcelAndJobInfoModel> getParcelAndJobInfo(@RequestHeader("Authorization") String token) {
        return ordersSenderService.getParcelAndJobInfo(token);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/get_active_parcels_not_in_job" })
    public ResponseEntity<List<OrdersDomain>> getActiveParcelsNotInJob(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize) {
        return ordersSenderService.getActiveParcelsNotInJob(token, pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/user_orders_count" })
    public ResponseEntity<Long> userOrdersCount(@RequestHeader("Authorization") String token) {
        return ordersSenderService.userOrdersCount(token);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/remove_parcel" })
    public ResponseEntity<Boolean> removeParcel(Long orderId) {
        return ordersSenderService.removeParcel(orderId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/get_jobs" })
    public ResponseEntity<JobsModel> getJobs(@RequestHeader("Authorization") String token) {
        return ordersSenderService.getJobs(token);
    }


    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/get_active_jobs" })
    public ResponseEntity<List<OrderJobsDomain>> getActiveJobs(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize) {
        return ordersSenderService.getActiveJobs(token, pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/get_on_hold_jobs" })
    public ResponseEntity<List<OrderJobsDomain>> getOnHoldJobs(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize) {
        return ordersSenderService.getOnHoldJobs(token, pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/get_done_jobs" })
    public ResponseEntity<List<OrderJobsDomain>> getDoneJobs(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize) {
        return ordersSenderService.getDoneJobs(token, pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/get_handed_over_jobs" })
    public ResponseEntity<List<OrderJobsDomain>> getHandedOverJobs(@RequestHeader("Authorization") String token, Integer pageKey, Integer pageSize) {
        return ordersSenderService.getHandedOverJobs(token, pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/remove_job" })
    public ResponseEntity<Boolean> removeJob(Long orderJobId) {
        return ordersSenderService.removeJob(orderJobId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/activate_job" })
    public ResponseEntity<Boolean> activateJob(Long orderJobId) {
        return ordersSenderService.activateJob(orderJobId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/hold_job" })
    public ResponseEntity<String> holdJob(Long orderJobId) {
        return ordersSenderService.holdJob(orderJobId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/orders_by_job" })
    public ResponseEntity<List<OrdersDomain>> ordersByJob(Long orderJobId, Integer pageKey, Integer pageSize) {
        return ordersSenderService.ordersByJob(orderJobId, pageKey, pageSize);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/remove_status_express" })
    public ResponseEntity<Boolean> removeStatusExpress(Long orderId) {
        return ordersSenderService.removeStatusExpress(orderId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/add_status_express" })
    public ResponseEntity<Boolean> addStatusExpress(Long orderId, String serviceDateFrom, String serviceDateTo, String serviceDate) {
        return ordersSenderService.addStatusExpress(orderId, serviceDateFrom, serviceDateTo, serviceDate);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/orders_couriers_info" })
    public ResponseEntity<List<OrdersCouriersInfoModel>> ordersCouriersInfo(@RequestHeader("Authorization") String token) {
        return ordersSenderService.ordersCouriersInfo(token);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/accept_handed_over_job" })
    public ResponseEntity<String> acceptHandedOverJob(Long orderJobId) {
        return ordersSenderService.handedOverJobDispenser(orderJobId, true);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/not_accept_handed_over_job" })
    public ResponseEntity<String> notAcceptHandedOverJob(Long orderJobId) {
        return ordersSenderService.handedOverJobDispenser(orderJobId, false);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/hand_over_job" })
    public ResponseEntity<Boolean> handOverJob(Long orderJobId, Integer newSenderUserId) {
        return ordersSenderService.handOverJob(orderJobId, newSenderUserId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/hand_over_jobs" })
    public ResponseEntity<Boolean> handOverJobs(String checkedJobs, Integer newSenderUserId) {
        return ordersSenderService.handOverJobs(checkedJobs, newSenderUserId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/hand_over_jobs_to_fav_courier_company" })
    public ResponseEntity<String> handOverJobsToFavCourierCompany(String checkedJobs) {
        return ordersSenderService.handOverJobsToFavCourierCompany(checkedJobs);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/hand_over_one_order_to_fav_courier_company"})
    public ResponseEntity<String> handOverOneOrderToFavCourierCompany(Long orderId) {
        return ordersSenderService.handOverOneOrderToFavCourierCompany(orderId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/hand_over_one_order_to_courier"})
    public ResponseEntity<String> handOverOneOrderToCourier(Long orderId, Long courierId) {
        return ordersSenderService.handOverOneOrderToCourier(orderId, courierId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/hand_over_job_to_courier" })
    public ResponseEntity<Boolean> handOverJobToCourier(@RequestHeader("Authorization") String token, Long orderJobId, Integer courierUserId) {
        return ordersSenderService.handOverJobToCourier(token, orderJobId, courierUserId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/remove_order_from_job" })
    public ResponseEntity<Boolean> removeOrderFromJob(@RequestHeader("Authorization") String token, Long orderId) {
        return ordersSenderService.removeOrderFromJob(token, orderId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/sent_order_to_another_job" })
    public ResponseEntity<Boolean> sentOrderToAnotherJob(@RequestHeader("Authorization") String token, Long orderId, Long newJobId) {
        return ordersSenderService.sentOrderToAnotherJob(token, orderId, newJobId);
    }

    @PreAuthorize("hasRole('ROLE_SENDER')")
    @RequestMapping({ "/update_job_name" })
    public ResponseEntity<Boolean> updateJobName(Long orderJobId, String jobName) {
        return ordersSenderService.updateJobName(orderJobId, jobName);
    }

}
