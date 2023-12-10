package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.models.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by Lazo on 2021-04-13
 */

public interface OrdersSenderService {

    ResponseEntity<String> getServicePrice(ServicePriceModel model);

    ResponseEntity<String> placeAnOrder(OrderModel model);

    ResponseEntity<Object> createJob(String token, String checkedParcels, Boolean active, Boolean hold);

    ResponseEntity<ParcelAndJobInfoModel> getParcelAndJobInfo(String token);

    ResponseEntity<List<OrdersDomain>> getActiveParcelsNotInJob(String token, Integer pageKey, Integer pageSize);

    ResponseEntity<Long> userOrdersCount(String token);

    ResponseEntity<Boolean> removeParcel(Long orderId);

    ResponseEntity<JobsModel> getJobs(String token);

    ResponseEntity<List<OrderJobsDomain>> getActiveJobs(String token, Integer pageKey, Integer pageSize);

    ResponseEntity<List<OrderJobsDomain>> getOnHoldJobs(String token, Integer pageKey, Integer pageSize);

    ResponseEntity<List<OrderJobsDomain>> getDoneJobs(String token, Integer pageKey, Integer pageSize);

    ResponseEntity<List<OrderJobsDomain>> getHandedOverJobs(String token, Integer pageKey, Integer pageSize);

    ResponseEntity<Boolean> removeJob(Long orderJobId);

    ResponseEntity<Boolean> activateJob(Long orderJobId);

    ResponseEntity<String> holdJob(Long orderJobId);

    ResponseEntity<List<OrdersDomain>> ordersByJob(Long orderJobId, Integer pageKey, Integer pageSize);

    ResponseEntity<Boolean> removeStatusExpress(Long orderId);

    ResponseEntity<Boolean> addStatusExpress(Long orderId, String serviceDateFrom, String serviceDateTo, String serviceDate);

    ResponseEntity<List<OrdersCouriersInfoModel>> ordersCouriersInfo(String token);

    ResponseEntity<String> handedOverJobDispenser(Long orderJobId, Boolean accept);

    ResponseEntity<Boolean> handOverJob(Long orderJobId, Integer newSenderUserId);

    ResponseEntity<Boolean> handOverJobs(String checkedJobs, Integer newSenderUserId);

    ResponseEntity<String> handOverJobsToFavCourierCompany(String checkedJobs);

    ResponseEntity<String> handOverOneOrderToFavCourierCompany(Long orderId);

    ResponseEntity<String> handOverOneOrderToCourier(Long orderId, Long courierId);

    ResponseEntity<Boolean> handOverJobToCourier(String token, Long orderJobId, Integer courierUserId);

    ResponseEntity<Boolean> removeOrderFromJob(String token, Long orderId);

    ResponseEntity<Boolean> sentOrderToAnotherJob(String token, Long orderId, Long newJobId);

    ResponseEntity<Boolean> updateJobName(Long orderJobId, String jobName);

}
