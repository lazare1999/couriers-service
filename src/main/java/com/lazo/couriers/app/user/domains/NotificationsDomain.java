package com.lazo.couriers.app.user.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by Lazo on 2022-05-24
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "users", name = "notifications")
public class NotificationsDomain {

    @Id
    @Column(name = "notification_id")
    @SequenceGenerator(name = "notifications_notification_id_seq", sequenceName = "users.notifications_notification_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_notification_id_seq")
    private Long notificationId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "must_rate_user")
    private Boolean mustRateUser;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "add_date", insertable = false, updatable = false)
    private LocalDateTime addDate;

    public NotificationsDomain(String title, String body, Long userId, Boolean mustRateUser) {
        this.userId = userId;
        this.statusId = 0;
        this.title = title;
        this.body = body;
        this.mustRateUser = mustRateUser;
    }

}
