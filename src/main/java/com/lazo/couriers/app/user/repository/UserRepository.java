package com.lazo.couriers.app.user.repository;

import com.lazo.couriers.app.user.domains.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Created by Lazo on 2021-02-11
 */

public interface UserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser> {

    AppUser findByUsername(String username);

    @Query("select u.userId from AppUser u where u.username = :username")
    Long findUserIdByUsername(@Param("username") String username);

    @Query("select u.username from AppUser u where u.userId = :userId")
    String findUsernameByUserId(@Param("userId") Long userId);

}
