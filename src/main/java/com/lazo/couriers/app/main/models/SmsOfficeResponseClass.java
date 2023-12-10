package com.lazo.couriers.app.main.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-02-15
 */

@Getter
@Setter
public class SmsOfficeResponseClass {

    private Boolean Success;
    private String  Message;
    private Object Output;
    private Integer ErrorCode;

}
