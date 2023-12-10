package com.lazo.couriers.app.user.repository;

import com.lazo.couriers.app.user.domains.TemporaryCodesDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Created by Lazo on 2021-05-17
 */

public interface TemporaryCodesRepository extends JpaRepository<TemporaryCodesDomain, Long>, JpaSpecificationExecutor<TemporaryCodesDomain> {

    Optional<TemporaryCodesDomain> findByUserName(String userName);

}
