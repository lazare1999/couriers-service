package com.lazo.couriers.app.orders.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-04-13
 */

@Getter
@Setter
public class ServicePriceModel {
    private Double totalDistance;
    private Boolean express;
    private String pickupAdminArea;
    private String toBeDeliveredAdminArea;
    private String pickupCountryCode;
    private String toBeDeliveredCountryCode;
    private Integer parcelType;
}
