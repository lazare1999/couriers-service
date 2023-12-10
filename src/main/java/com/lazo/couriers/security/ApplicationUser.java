package com.lazo.couriers.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Created by Lazo on 2021-10-04
 */

@Getter
@Setter
public class ApplicationUser extends User  {

    private final Integer userId;

    public ApplicationUser(Integer userId, String username, String password, boolean enabled,
                           boolean accountNonExpired, boolean credentialsNonExpired,
                           boolean accountNonLocked,
                           Collection<GrantedAuthority> authorities) {

        super(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities);

        this.userId = userId;
    }


}
