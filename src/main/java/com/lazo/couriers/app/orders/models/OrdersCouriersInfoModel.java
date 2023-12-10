package com.lazo.couriers.app.orders.models;

import com.lazo.couriers.app.orders.domain.OrdersDomain;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2021-05-31
 */

@Getter
@Setter
public class OrdersCouriersInfoModel {
    private Long id;
    private String courierPhone;
    private String parcelAddressToBeDeliveredLatitude;
    private String parcelAddressToBeDeliveredLongitude;

    public void update(OrdersDomain d, String courierPhone) {
        this.id = d.getOrderId();
        this.courierPhone = courierPhone !=null ? courierPhone : "";
        this.parcelAddressToBeDeliveredLatitude = d.getParcelAddressToBeDeliveredLatitude();
        this.parcelAddressToBeDeliveredLongitude = d.getParcelAddressToBeDeliveredLongitude();
    }
}
