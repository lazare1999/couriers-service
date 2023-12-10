package com.lazo.couriers.app.orders.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-05-24
 */

@Getter
@Setter
public class JobsModel {

    private Long activeJobs;
    private Long onHoldJobs;
    private Long doneJobs;
    private Long handedOverJobs;
}
