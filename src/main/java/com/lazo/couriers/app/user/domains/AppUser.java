package com.lazo.couriers.app.user.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by Lazo on 2021-02-11
 */


@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "users", name = "active_users")
public class AppUser {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "email")
    private String email;

    @Column(name = "rating")
    private Double rating;

}
