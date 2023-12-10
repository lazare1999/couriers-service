package com.lazo.couriers.app.user.repository;

import com.lazo.couriers.app.user.domains.NotificationsDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by Lazo on 2022-05-24
 */

public interface NotificationsRepository extends JpaRepository<NotificationsDomain, Long>, JpaSpecificationExecutor<NotificationsDomain> {
}
