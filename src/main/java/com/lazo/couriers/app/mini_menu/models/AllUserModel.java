package com.lazo.couriers.app.mini_menu.models;

import com.lazo.couriers.app.user.domains.AppUser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Created by Lazo on 2021-06-03
 */

@Getter
@Setter
@RequiredArgsConstructor
public class AllUserModel {

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private Double rating;
    private String mainNickname;
    private String nickname;
    private Boolean isFav;

    public AllUserModel(AppUser u, String nickname, Boolean isFav) {
        this.userId = u.getUserId();
        this.username = u.getUsername();
        this.firstName = u.getFirstName();
        this.lastName = u.getLastName();
        this.rating = u.getRating();
        this.mainNickname = u.getNickname();
        this.nickname = nickname;
        this.isFav = isFav;
    }

}
