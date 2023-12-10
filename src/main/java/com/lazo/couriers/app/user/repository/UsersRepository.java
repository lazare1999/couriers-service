package com.lazo.couriers.app.user.repository;

import com.lazo.couriers.app.user.domains.UsersDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by Lazo on 2022-01-24
 */

public interface UsersRepository extends JpaRepository<UsersDomain, Long>, JpaSpecificationExecutor<UsersDomain> {

    @Query("select u.deposit from UsersDomain u where u.userId = :userId")
    Double depositByUserId(@Param("userId") Long userId);

}
