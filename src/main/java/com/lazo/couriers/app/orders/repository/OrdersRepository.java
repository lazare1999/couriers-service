package com.lazo.couriers.app.orders.repository;

import com.lazo.couriers.app.orders.domain.OrdersDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Lazo on 2021-04-13
 */

public interface OrdersRepository extends JpaRepository<OrdersDomain, Long>, JpaSpecificationExecutor<OrdersDomain> {

    List<OrdersDomain> findAllBySenderUserIdAndOrderStatusAndJobIdOrderByOrderId(Integer senderUserId, OrdersDomain.OrderStatus status, Long jobId);

    @Modifying
    @Query("delete from OrdersDomain o where o.jobId = :orderJobId")
    void deleteByOrderJobId(@Param("orderJobId") Long orderJobId);

    List<OrdersDomain> findAllByJobId(Long jobId);
}
