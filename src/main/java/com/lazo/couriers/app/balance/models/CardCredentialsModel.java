package com.lazo.couriers.app.balance.models;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Lazo on 2022-04-06
 */

@Getter
@Setter
public class CardCredentialsModel {

    private String cardNumber;
    private String expiryDate;
    private String cardHolderName;
    private String cvvCode;

    public CardCredentialsModel(String cardNumber, String expiryDate, String cardHolderName, String cvvCode) {
        this.cardNumber =cardNumber;
        this.expiryDate =expiryDate;
        this.cardHolderName =cardHolderName;
        this.cvvCode =cvvCode;
    }

}
