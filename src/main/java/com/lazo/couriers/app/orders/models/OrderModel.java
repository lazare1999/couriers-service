package com.lazo.couriers.app.orders.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-04-13
 */

@Getter
@Setter
public class OrderModel {

    private String express;
    private String parcelPickupAddressLatitude;
    private String parcelPickupAddressLongitude;
    private String parcelAddressToBeDeliveredLatitude;
    private String parcelAddressToBeDeliveredLongitude;
    private String servicePrice;
    private String servicePaymentType;
    private String clientName;
    private String serviceDate;
    private String serviceDateFrom;
    private String serviceDateTo;
    private String serviceParcelPrice;
    private String serviceParcelIdentifiable;
    private String orderComment;
    private String pickupAdminArea;
    private String pickupCountryCode;
    private String toBeDeliveredAdminArea;
    private String toBeDeliveredCountryCode;
    private String totalDistance;
    private String viewerPhone;
    private Integer parcelType;
    private Boolean courierHasParcelMoney;

}
