package com.lazo.couriers.app.accounting.repository;

import com.lazo.couriers.app.accounting.domains.DebtsDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Created by Lazo on 2022-01-24
 */

public interface DebtsRepository extends JpaRepository<DebtsDomain, Long>, JpaSpecificationExecutor<DebtsDomain> {


    @Query("select d.cardDebt from DebtsDomain d where d.userId = :userId and d.paid = :paid and d.cardDebt is not null")
    List<Double> cardDebtsList(@Param("userId") Long userId, @Param("paid") Boolean paid);

    @Query("select d from DebtsDomain d where d.userId = :userId and d.paid = :paid and d.cardDebt is not null")
    List<DebtsDomain> cardDebtsListObj(@Param("userId") Long userId, @Param("paid") Boolean paid);

    @Query("select d.depositDebt from DebtsDomain d where d.userId = :userId and d.paid = :paid and d.depositDebt is not null")
    List<Double> depositDebtList(@Param("userId") Long userId, @Param("paid") Boolean paid);

}
