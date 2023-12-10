package com.lazo.couriers.app.user.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Created by Lazo on 2022-01-24
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "users", name = "users")
public class UsersDomain {

    public enum PaymentType {
        NORMAL, MONTHLY, EVERY_3_MONTH, EVERY_6_MONTH, EVERY_12_MONTH
    }

    @Id
    @Column(name = "user_id")
    @SequenceGenerator(name = "users_user_id_seq", sequenceName = "users.users_user_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_user_id_seq")
    private Long userId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "email")
    private String email;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "nickname")
    private String nickname;

    //    TODO : გადაარქვი სერვერის ბაზაშიც
    @Column(name = "favourite_courier_company_id")
    private Integer favouriteCourierCompanyId;

    //    TODO : გადაარქვი სერვერის ბაზაშიც
    @Column(name = "deposit")
    private Double deposit;

    //    TODO : გადაარქვი სერვერის ბაზაშიც
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "payment_type")
    private PaymentType paymentType;

    //    TODO : გადაარქვი სერვერის ბაზაშიც
    @Column(name = "is_vip")
    private Boolean isVip;

    //    TODO : გადაარქვი სერვერის ბაზაშიც
    @Column(name = "vip_expiration_date")
    private LocalDateTime vipExpirationDateTime;

}
