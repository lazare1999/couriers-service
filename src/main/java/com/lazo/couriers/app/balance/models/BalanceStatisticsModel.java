package com.lazo.couriers.app.balance.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2022-02-10
 */

@Getter
@Setter
public class BalanceStatisticsModel {

    private String parcelsAsCourier;
    private String parcelsAsSender;
    private String servicePrice;
    private String serviceParcelPriceSender;
    private String serviceParcelPriceCourier;
    private String ourShare;
    private String cardDebt;
    private String depositDebt;

    public BalanceStatisticsModel(String parcelsAsCourier, String parcelsAsSender, String servicePrice, String serviceParcelPriceSender, String serviceParcelPriceCourier, String ourShare, String cardDebt, String depositDebt) {

        this.parcelsAsCourier = parcelsAsCourier;
        this.parcelsAsSender = parcelsAsSender;
        this.servicePrice = servicePrice;
        this.serviceParcelPriceSender = serviceParcelPriceSender;
        this.serviceParcelPriceCourier = serviceParcelPriceCourier;
        this.ourShare = ourShare;
        this.cardDebt = cardDebt;
        this.depositDebt = depositDebt;

    }

}
