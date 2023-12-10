package com.lazo.couriers.app.balance.service;

import com.lazo.couriers.app.accounting.domains.DebtsDomain;
import com.lazo.couriers.app.balance.domains.UserCardsDomain;
import com.lazo.couriers.app.balance.models.BalanceStatisticsModel;
import com.lazo.couriers.app.balance.models.CardCredentialsModel;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by Lazo on 2022-02-10
 */

public interface BalanceService {

    ResponseEntity<BalanceStatisticsModel> getBalanceStatistics(String selectedDateFrom, String selectedDateTo);

    ResponseEntity<String> getDeposit();

    ResponseEntity<List<DebtsDomain>> getCardDebts(Integer pageKey, Integer pageSize, Boolean card, Boolean deposit);

    ResponseEntity<String> payDepositDebt();

    ResponseEntity<String> payCardDebt();

    ResponseEntity<Boolean> setCardCredentials(CardCredentialsModel model);

    ResponseEntity<List<UserCardsDomain>> getUserCardCredentials();
}
