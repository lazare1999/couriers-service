package com.lazo.couriers.app.balance.repository;

import com.lazo.couriers.app.balance.domains.UserCardsDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by Lazo on 2021-04-13
 */

public interface UserCardsRepository extends JpaRepository<UserCardsDomain, Long>, JpaSpecificationExecutor<UserCardsDomain> {

    List<UserCardsDomain> findAllByUserId(Long userId);
}
