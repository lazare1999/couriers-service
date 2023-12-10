package com.lazo.couriers.app.orders.repository;

import com.lazo.couriers.app.orders.domain.OrderJobsDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Lazo on 2021-04-13
 */

public interface OrderJobsRepository extends JpaRepository<OrderJobsDomain, Long>, JpaSpecificationExecutor<OrderJobsDomain> {

    @Query("select max (o.orderJobId) from OrderJobsDomain o")
    Long findMaxId();

    @Query("select o.courierPhone from OrderJobsDomain o where o.orderJobId = :jobId")
    String getCourierPhone(@Param("jobId") Long jobId);

    @Query("select o.orderJobId from OrderJobsDomain o where o.orderJobStatus =0 and  o.courierUserId = :courierUserId")
    List<Long> getOrderJobsIdsViaCourierUserId(@Param("courierUserId") Integer courierUserId);

    @Query("select o.orderJobId from OrderJobsDomain o where o.orderJobStatus =0 and o.courierUserId is null")
    List<Long> getOrderJobsIdsWhereCourierIdIsNull();

    @Query("select o from OrderJobsDomain o where o.orderJobStatus =0 and o.courierUserId is null and o.orderJobId = :orderJobId")
    OrderJobsDomain getFreeToTakeOrderByOrderId(@Param("orderJobId") Long orderJobId);

    @Query("select o.courierUserId from OrderJobsDomain o where o.orderJobId = :jobId")
    Long getCourierUserId(@Param("jobId") Long jobId);
}
