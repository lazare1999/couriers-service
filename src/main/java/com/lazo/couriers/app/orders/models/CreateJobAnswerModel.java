package com.lazo.couriers.app.orders.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-04-14
 */

@Getter
@Setter
public class CreateJobAnswerModel {

    private String answerVariable;
    private String orderCount;

    public CreateJobAnswerModel(String answerVariable, String orderCount) {
        this.answerVariable = answerVariable;
        this.orderCount = orderCount;
    }
}
