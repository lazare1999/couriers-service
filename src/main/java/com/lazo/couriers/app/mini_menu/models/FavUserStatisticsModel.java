package com.lazo.couriers.app.mini_menu.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-06-03
 */

@Getter
@Setter
public class FavUserStatisticsModel {

    private Long userDeliveredParcelsCount;
    private Long userSuccessfullyCompletedJobsCount;

    public FavUserStatisticsModel(long userDeliveredParcelsCount, long userSuccessfullyCompletedJobsCount) {
        this.userDeliveredParcelsCount = userDeliveredParcelsCount;
        this.userSuccessfullyCompletedJobsCount = userSuccessfullyCompletedJobsCount;
    }

}
