package com.lazo.couriers.app.user.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by Lazo on 2021-05-17
 */

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(schema = "users", name = "temporary_codes")
public class TemporaryCodesDomain {

    @Id
    @SequenceGenerator(name = "users.temporary_codes_temporary_code_id_seq", sequenceName = "users.temporary_codes_temporary_code_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users.temporary_codes_temporary_code_id_seq")
    @Column(name = "temporary_code_id")
    private Long temporaryCodeId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "code")
    private String code;

    public TemporaryCodesDomain(String username, String code) {
        this.userName = username;
        this.code = code;
    }
}
