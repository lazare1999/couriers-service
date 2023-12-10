package com.lazo.couriers.app.accounting.repository;

import com.lazo.couriers.app.accounting.domains.CourierFeesDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by Lazo on 2022-03-02
 */

public interface CourierFeesRepository extends JpaRepository<CourierFeesDomain, Long>, JpaSpecificationExecutor<CourierFeesDomain> {
}
