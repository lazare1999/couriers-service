package com.lazo.couriers.app.orders.service;

import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import com.lazo.couriers.app.orders.domain.OrdersDomain;
import com.lazo.couriers.app.orders.models.JobsModel;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by Lazo on 2021-10-08
 */

public interface OrdersCourierService {

    ResponseEntity<JobsModel> getCourierJobs(String token);

    ResponseEntity<List<OrderJobsDomain>> getActiveCourierJobs(String token, Integer pageKey, Integer pageSize);

    ResponseEntity<List<OrderJobsDomain>> getDoneCourierJobs(String token, Integer pageKey, Integer pageSize);

    ResponseEntity<List<OrderJobsDomain>> getHandedOverJobsCourier(String token, Integer pageKey, Integer pageSize);

    ResponseEntity<String> handedOverJobDispenserForCourier(Long orderJobId, Boolean accept);

    ResponseEntity<List<OrdersDomain>> getParcels(String token, boolean courierParcelsOnly);

    ResponseEntity<List<OrdersDomain>> getParcelsByJob(Integer pageKey, Integer pageSize, Long orderJobId);

    ResponseEntity<String> approveJob(Long orderJobId);

    ResponseEntity<List<OrderJobsDomain>> getVacantJobs();

    ResponseEntity<Boolean> setParcelAsUnsuccessful(Long orderId);

    ResponseEntity<Boolean> takeParcel(Long orderId);

    ResponseEntity<Boolean> jobsDone(Long orderId);

}
