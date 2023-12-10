package com.lazo.couriers.app.user.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by Lazo on 2021-06-01
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "users", name = "users_favorite_users")
public class UsersFavoriteUsersDomain {

    @Id
    @Column(name = "users_favorite_users_id")
    @SequenceGenerator(name = "users_favorite_users_users_favorite_users_id_seq", sequenceName = "users.users_favorite_users_users_favorite_users_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_favorite_users_users_favorite_users_id_seq")
    private Long usersFavoriteUsersId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "favorite_user_id")
    private Long favoriteUserId;

    @Column(name = "favorite_nickname")
    private String favoriteNickname;

}
