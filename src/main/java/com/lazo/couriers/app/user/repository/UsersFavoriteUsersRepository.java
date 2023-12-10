package com.lazo.couriers.app.user.repository;

import com.lazo.couriers.app.user.domains.UsersFavoriteUsersDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Created by Lazo on 2021-06-01
 */

public interface UsersFavoriteUsersRepository extends JpaRepository<UsersFavoriteUsersDomain, Long>, JpaSpecificationExecutor<UsersFavoriteUsersDomain> {

    List<UsersFavoriteUsersDomain> findAllByUserId(Long userId);

    @Modifying
    @Query("delete from UsersFavoriteUsersDomain f where f.userId = :userId and f.favoriteUserId = :favoriteUserId")
    void deleteByUserIdAndFavoriteUserId(@Param("userId") Long userId, @Param("favoriteUserId") Long favoriteUserId);

    Optional<UsersFavoriteUsersDomain> findByUserIdAndFavoriteUserId(Long userId, Long favoriteUserId);

    @Query("select f.favoriteNickname from UsersFavoriteUsersDomain f where f.userId = :userId and f.favoriteUserId = :favoriteUserId")
    String nickname(@Param("userId") Long userId, @Param("favoriteUserId") Long favoriteUserId);
}
