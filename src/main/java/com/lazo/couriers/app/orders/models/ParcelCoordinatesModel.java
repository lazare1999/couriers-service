package com.lazo.couriers.app.orders.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-06-16
 */

@Getter
@Setter
public class ParcelCoordinatesModel {
    private String takeawayLatitude;
    private String takeawayLongitude;
    private String deliveryLatitude;
    private String deliveryLongitude;
}
