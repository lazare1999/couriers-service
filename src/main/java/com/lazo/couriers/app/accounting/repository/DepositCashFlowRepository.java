package com.lazo.couriers.app.accounting.repository;

import com.lazo.couriers.app.accounting.domains.DepositCashFlowDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by Lazo on 2022-03-04
 */

public interface DepositCashFlowRepository extends JpaRepository<DepositCashFlowDomain, Long>, JpaSpecificationExecutor<DepositCashFlowDomain> {
}
