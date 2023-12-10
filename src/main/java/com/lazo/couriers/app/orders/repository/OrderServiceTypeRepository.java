package com.lazo.couriers.app.orders.repository;

import com.lazo.couriers.app.orders.domain.OrderServiceTypeDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by Lazo on 2021-04-13
 */

public interface OrderServiceTypeRepository extends JpaRepository<OrderServiceTypeDomain, Long>, JpaSpecificationExecutor<OrderServiceTypeDomain> {
}
